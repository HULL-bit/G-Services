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
 * {@code Article} — déclinaison concrète et vendable d'un {@link Produit}
 * (référence, prix, remise éventuelle, disponibilité). Ses valeurs de propriétés
 * sont portées par {@link VarieteArticle}.
 */
@Entity
@Table(name = "article", indexes = {
        @Index(name = "idx_article_produit", columnList = "id_produit"),
        @Index(name = "idx_article_reference", columnList = "reference", unique = true)
})
@Getter
@Setter
public class Article extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_article")
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(name = "reference", nullable = false, unique = true, length = 60)
    private String reference;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "prix", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal prix = java.math.BigDecimal.ZERO;

    @Size(max = 10)
    @Column(name = "devise", length = 10)
    private String devise = "XOF";

    /** UNITE, LOT, POIDS, VOLUME, HEURE… */
    @Size(max = 20)
    @Column(name = "mode_vente", length = 20)
    private String modeVente;

    @Size(max = 160)
    @Column(name = "image", length = 160)
    private String image;

    @Column(name = "disponibilite", nullable = false)
    private boolean disponibilite = true;

    @Column(name = "promotion", nullable = false)
    private boolean promotion = false;

    @Column(name = "taux_remise_pourcentage", precision = 5, scale = 2)
    private java.math.BigDecimal tauxRemisePourcentage = java.math.BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "date", nullable = false, updatable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_produit", nullable = false,
            foreignKey = @ForeignKey(name = "fk_article_produit"))
    private Produit produit;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VarieteArticle> varietes = new LinkedHashSet<>();

    /** Stock de cet article (relation 1-1, cf. {@link Stock}) — {@code null} tant qu'aucun stock n'est ouvert. */
    @OneToOne(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stock stock;

    public void addVariete(VarieteArticle v) {
        varietes.add(v);
        v.setArticle(this);
    }

    /** Prix effectivement payé : remise appliquée si {@code promotion}. */
    @Transient
    public java.math.BigDecimal getPrixNet() {
        if (promotion && tauxRemisePourcentage != null
                && tauxRemisePourcentage.signum() > 0) {
            java.math.BigDecimal facteur = java.math.BigDecimal.ONE
                    .subtract(tauxRemisePourcentage.movePointLeft(2));
            return prix.multiply(facteur).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return prix;
    }
}
