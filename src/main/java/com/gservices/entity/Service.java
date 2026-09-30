package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code Service} — arborescent (auto-référence {@code sous-catégorie}) et rattaché
 * à une {@link CategorieService}. Peut proposer un ou plusieurs {@link Catalogue}.
 */
@Entity
@Table(name = "service", indexes = {
        @Index(name = "idx_service_categorie", columnList = "id_categorie_service"),
        @Index(name = "idx_service_parent", columnList = "id_service_parent"),
        @Index(name = "idx_service_libelle", columnList = "libelle")
})
@Getter
@Setter
public class Service extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "libelle", nullable = false, length = 120)
    private String libelle;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 160)
    @Column(name = "image", length = 160)
    private String image;

    @Column(name = "prix_indicatif")
    private Double prixIndicatif;

    /**
     * {@code true} si ce service accepte les <strong>commandes à distance</strong>
     * (vente en ligne). Sinon le client doit se rendre sur place — la fiche
     * publique n'affiche alors pas de tunnel de commande. Hors UML.
     */
    @Column(name = "commande_a_distance", nullable = false)
    private boolean commandeADistance = false;

    /**
     * Validation du service par un administrateur (hors UML). Un service créé par
     * un fournisseur reste {@code false} — invisible sur le front public — jusqu'à
     * validation. Les services créés depuis le back-office sont {@code true}.
     */
    @Column(name = "valide", nullable = false)
    private boolean valide = true;

    /**
     * Blocage administratif suite à un signalement (§3 « Système de sanction »,
     * hors UML). Un service bloqué disparaît du front public sans être supprimé ;
     * l'historique des sanctions est journalisé dans {@link Sanction}.
     */
    @Column(name = "bloque", nullable = false)
    private boolean bloque = false;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categorie_service", nullable = false,
            foreignKey = @ForeignKey(name = "fk_service_categorie"))
    private CategorieService categorieService;

    /** Service parent (sous-catégorie) — {@code null} si racine. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_service_parent",
            foreignKey = @ForeignKey(name = "fk_service_parent"))
    private Service parent;

    /**
     * Propriétaire / prestataire qui paramètre ce service (association
     * « paramètre (admin) » du diagramme). {@code null} pour un service géré
     * uniquement par l'administration.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proprietaire",
            foreignKey = @ForeignKey(name = "fk_service_proprietaire"))
    private Personne proprietaire;

    @OneToMany(mappedBy = "parent")
    private Set<Service> sousServices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "service")
    private Set<Catalogue> catalogues = new LinkedHashSet<>();

    /** Fiche prestataire (relation 1-1, cf. {@link InformationService}). */
    @OneToOne(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private InformationService informationService;
}
