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
 * {@code Continent} — paquetage <strong>GestServices</strong>. Racine de la
 * hiérarchie géographique : {@code Continent → ZoneGeographique → Pays → Region → Ville → Position}.
 */
@Entity
@Table(name = "continent", indexes = {
        @Index(name = "idx_continent_code", columnList = "code", unique = true),
        @Index(name = "idx_continent_libelle", columnList = "libelle")
})
@Getter
@Setter
public class Continent extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_continent")
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "libelle", nullable = false, length = 80)
    private String libelle;

    @Size(max = 10)
    @Column(name = "code", unique = true, length = 10)
    private String code;

    @Column(name = "superficie_km2")
    private Double superficieKm2;

    @Column(name = "population")
    private Long population;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "continent")
    private Set<ZoneGeographique> zones = new LinkedHashSet<>();
}
