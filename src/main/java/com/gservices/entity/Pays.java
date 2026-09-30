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

/** {@code Pays} — appartient à une {@link ZoneGeographique}. */
@Entity
@Table(name = "pays", indexes = {
        @Index(name = "idx_pays_iso2", columnList = "code_iso2", unique = true),
        @Index(name = "idx_pays_iso3", columnList = "code_iso3", unique = true),
        @Index(name = "idx_pays_zone", columnList = "id_zone_geographique"),
        @Index(name = "idx_pays_libelle", columnList = "libelle")
})
@Getter
@Setter
public class Pays extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pays")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Size(max = 2)
    @Column(name = "code_iso2", unique = true, length = 2)
    private String codeIso2;

    @Size(max = 3)
    @Column(name = "code_iso3", unique = true, length = 3)
    private String codeIso3;

    @Size(max = 10)
    @Column(name = "indicatif_telephonique", length = 10)
    private String indicatifTelephonique;

    @Size(max = 10)
    @Column(name = "devise", length = 10)
    private String devise;

    @Size(max = 100)
    @Column(name = "capitale", length = 100)
    private String capitale;

    @Size(max = 80)
    @Column(name = "langue_officielle", length = 80)
    private String langueOfficielle;

    @Column(name = "superficie_km2")
    private Double superficieKm2;

    @Column(name = "population")
    private Long population;

    /** Nom de fichier du drapeau (dossier {@code resources/media/flags/}). */
    @Size(max = 120)
    @Column(name = "drapeau", length = 120)
    private String drapeau;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_zone_geographique", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pays_zone"))
    private ZoneGeographique zoneGeographique;

    @OneToMany(mappedBy = "pays")
    private Set<Region> regions = new LinkedHashSet<>();
}
