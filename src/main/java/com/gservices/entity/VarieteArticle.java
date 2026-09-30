package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code VarieteArticle} — <strong>classe d'association</strong> {@link Article} ×
 * {@link ProprietesArticle} : la valeur prise par une propriété pour un article donné
 * (ex. article « T-shirt bleu M » → propriété « Couleur » = « Bleu », « Taille » = « M »).
 */
@Entity
@Table(name = "variete_article", uniqueConstraints = @UniqueConstraint(
        name = "uk_variete_article", columnNames = {"id_article", "id_proprietes_article"}),
        indexes = {
                @Index(name = "idx_variete_article_article", columnList = "id_article"),
                @Index(name = "idx_variete_article_propriete", columnList = "id_proprietes_article")
        })
@Getter
@Setter
public class VarieteArticle extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variete_article")
    private Long id;

    @Size(max = 255)
    @Column(name = "valeur", length = 255)
    private String valeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_article", nullable = false,
            foreignKey = @ForeignKey(name = "fk_variete_article_article"))
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proprietes_article", nullable = false,
            foreignKey = @ForeignKey(name = "fk_variete_article_propriete"))
    private ProprietesArticle proprietesArticle;
}
