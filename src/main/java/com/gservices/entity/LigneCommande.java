package com.gservices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Ligne d'une {@link Commande} : un {@link Article}, une quantité et le prix
 * unitaire figé au moment de la commande (le prix de l'article peut évoluer
 * ensuite).
 */
@Entity
@Table(name = "ligne_commande", indexes = {
        @Index(name = "idx_ligne_commande_commande", columnList = "id_commande")
})
@Getter
@Setter
public class LigneCommande extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_commande")
    private Long id;

    @Column(name = "quantite", nullable = false)
    private int quantite = 1;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commande", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ligne_commande_commande"))
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_article", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ligne_commande_article"))
    private Article article;

    @Transient
    public BigDecimal getSousTotal() {
        return prixUnitaire == null ? BigDecimal.ZERO
                : prixUnitaire.multiply(BigDecimal.valueOf(Math.max(0, quantite)));
    }
}
