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

/** {@code Region} — subdivision d'un {@link Pays}. */
@Entity
@Table(name = "region", indexes = {
        @Index(name = "idx_region_code", columnList = "code_region"),
        @Index(name = "idx_region_pays", columnList = "id_pays"),
        @Index(name = "idx_region_libelle", columnList = "libelle")
})
@Getter
@Setter
public class Region extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_region")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Size(max = 20)
    @Column(name = "code_region", length = 20)
    private String codeRegion;

    @Size(max = 100)
    @Column(name = "chef_lieu", length = 100)
    private String chefLieu;

    @Column(name = "superficie_km2")
    private Double superficieKm2;

    @Column(name = "population")
    private Long population;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pays", nullable = false,
            foreignKey = @ForeignKey(name = "fk_region_pays"))
    private Pays pays;

    @OneToMany(mappedBy = "region")
    private Set<Ville> villes = new LinkedHashSet<>();
}
