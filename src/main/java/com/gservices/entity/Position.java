package com.gservices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * {@code Position} — point géographique (GPS) rattaché à une {@link Ville}.
 * Le lien vers {@code InformationService} (prestataire) est ajouté au Lot 4.
 */
@Entity
@Table(name = "position", indexes = {
        @Index(name = "idx_position_ville", columnList = "id_ville")
})
@Getter
@Setter
public class Position extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_position")
    private Long id;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "precision_metres")
    private Double precisionMetres;

    @Column(name = "date_releve")
    private LocalDate dateReleve;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ville", nullable = false,
            foreignKey = @ForeignKey(name = "fk_position_ville"))
    private Ville ville;
}
