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

/** {@code Produit} — regroupé dans un {@link Catalogue}, décliné en {@link Article}. */
@Entity
@Table(name = "produit", indexes = {
        @Index(name = "idx_produit_catalogue", columnList = "id_catalogue"),
        @Index(name = "idx_produit_libelle", columnList = "libelle")
})
@Getter
@Setter
public class Produit extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produit")
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "libelle", nullable = false, length = 150)
    private String libelle;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 160)
    @Column(name = "image", length = 160)
    private String image;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_catalogue", nullable = false,
            foreignKey = @ForeignKey(name = "fk_produit_catalogue"))
    private Catalogue catalogue;

    @OneToMany(mappedBy = "produit")
    private Set<Article> articles = new LinkedHashSet<>();

    /** Propriétés que ce produit déclare (association « définit », n-n). */
    @ManyToMany
    @JoinTable(name = "produit_proprietes_article",
            joinColumns = @JoinColumn(name = "id_produit"),
            inverseJoinColumns = @JoinColumn(name = "id_proprietes_article"),
            foreignKey = @ForeignKey(name = "fk_ppa_produit"),
            inverseForeignKey = @ForeignKey(name = "fk_ppa_proprietes"))
    private Set<ProprietesArticle> proprietes = new LinkedHashSet<>();
}
