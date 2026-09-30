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

/** {@code Ville} — appartient à une {@link Region} ; localisée par une ou plusieurs {@link Position}. */
@Entity
@Table(name = "ville", indexes = {
        @Index(name = "idx_ville_region", columnList = "id_region"),
        @Index(name = "idx_ville_libelle", columnList = "libelle"),
        @Index(name = "idx_ville_cp", columnList = "code_postal")
})
@Getter
@Setter
public class Ville extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ville")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Size(max = 20)
    @Column(name = "code_postal", length = 20)
    private String codePostal;

    @Size(max = 20)
    @Column(name = "indicatif_zone", length = 20)
    private String indicatifZone;

    @Column(name = "population")
    private Long population;

    @Column(name = "superficie_km2")
    private Double superficieKm2;

    @Column(name = "est_capitale", nullable = false)
    private boolean estCapitale = false;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_region", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ville_region"))
    private Region region;

    @OneToMany(mappedBy = "ville", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Position> positions = new LinkedHashSet<>();

    public void addPosition(Position position) {
        positions.add(position);
        position.setVille(this);
    }
}
