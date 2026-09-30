package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * {@code Signalement} — <strong>classe d'association</strong> {@link Personne} ×
 * {@link Service} (hors UML, cf. cahier « Système de sanction / surveillance de
 * la plateforme »). Un client signale un service ; un administrateur l'examine
 * et soit le rejette, soit prononce une {@link Sanction} (blocage du service).
 */
@Entity
@Table(name = "signalement", indexes = {
        @Index(name = "idx_signalement_service", columnList = "id_service"),
        @Index(name = "idx_signalement_statut", columnList = "statut")
})
@Getter
@Setter
public class Signalement extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signalement")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "motif", nullable = false, length = 40)
    private MotifSignalement motif;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "date", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutSignalement statut = StatutSignalement.NOUVEAU;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_personne", nullable = false, foreignKey = @ForeignKey(name = "fk_signalement_personne"))
    private Personne personne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false, foreignKey = @ForeignKey(name = "fk_signalement_service"))
    private Service service;
}
