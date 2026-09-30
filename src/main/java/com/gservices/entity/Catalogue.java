package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/** {@code Catalogue} — proposé par un {@link Service}, regroupe des {@link Produit}. */
@Entity
@Table(name = "catalogue", indexes = {
        @Index(name = "idx_catalogue_service", columnList = "id_service"),
        @Index(name = "idx_catalogue_libelle", columnList = "libelle")
})
@Getter
@Setter
public class Catalogue extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_catalogue")
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "libelle", nullable = false, length = 120)
    private String libelle;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 60)
    @Column(name = "icone", length = 60)
    private String icone;

    @Column(name = "ordre_affichage", nullable = false)
    private int ordreAffichage = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false,
            foreignKey = @ForeignKey(name = "fk_catalogue_service"))
    private Service service;

    @OneToMany(mappedBy = "catalogue")
    private Set<Produit> produits = new LinkedHashSet<>();
}
