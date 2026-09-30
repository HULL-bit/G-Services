package com.gservices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code Commande} — <strong>hors diagramme UML</strong>.
 *
 * <p>Matérialise la règle de gestion « certains services acceptent la
 * <em>commande à distance</em> » ({@link Service#isCommandeADistance()}) : un
 * {@link Personne client} commande un ou plusieurs {@link Article} d'un
 * {@link Service} donné. Le détail est porté par les {@link LigneCommande}.</p>
 */
@Entity
@Table(name = "commande", indexes = {
        @Index(name = "idx_commande_client", columnList = "id_client"),
        @Index(name = "idx_commande_service", columnList = "id_service"),
        @Index(name = "idx_commande_statut", columnList = "statut")
})
@Getter
@Setter
public class Commande extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private Long id;

    /** Référence lisible, ex. {@code CMD-000042}. */
    @Column(name = "reference", nullable = false, unique = true, length = 30)
    private String reference;

    @CreationTimestamp
    @Column(name = "date_commande", nullable = false, updatable = false)
    private LocalDateTime dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.NOUVELLE;

    @Column(name = "montant_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "commentaire", length = 1000)
    private String commentaire;

    @Column(name = "adresse_livraison", length = 255)
    private String adresseLivraison;

    @Column(name = "telephone_contact", length = 30)
    private String telephoneContact;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_client", nullable = false,
            foreignKey = @ForeignKey(name = "fk_commande_client"))
    private Personne client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false,
            foreignKey = @ForeignKey(name = "fk_commande_service"))
    private Service service;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LigneCommande> lignes = new LinkedHashSet<>();

    public void addLigne(LigneCommande ligne) {
        lignes.add(ligne);
        ligne.setCommande(this);
    }

    /** Recalcule {@link #montantTotal} depuis les lignes actives. */
    public void recalculerTotal() {
        this.montantTotal = lignes.stream()
                .filter(LigneCommande::isEtat)
                .map(LigneCommande::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public int getNombreArticles() {
        return lignes.stream().filter(LigneCommande::isEtat)
                .mapToInt(LigneCommande::getQuantite).sum();
    }
}
