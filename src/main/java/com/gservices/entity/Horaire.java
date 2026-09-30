package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code Horaire} — créneau d'ouverture d'un prestataire ({@link InformationService})
 * pour un {@link Jour} de la semaine.
 */
@Entity
@Table(name = "horaire", uniqueConstraints = @UniqueConstraint(
        name = "uk_horaire_info_jour", columnNames = {"id_information_service", "id_jour"}),
        indexes = {
                @Index(name = "idx_horaire_info", columnList = "id_information_service"),
                @Index(name = "idx_horaire_jour", columnList = "id_jour")
        })
@Getter
@Setter
public class Horaire extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horaire")
    private Long id;

    @Size(max = 5)
    @Column(name = "heure_ouverture", length = 5)
    private String heureOuverture;

    @Size(max = 5)
    @Column(name = "heure_fermeture", length = 5)
    private String heureFermeture;

    @Size(max = 5)
    @Column(name = "pause_dejeuner_debut", length = 5)
    private String pauseDejeunerDebut;

    @Size(max = 5)
    @Column(name = "pause_dejeuner_fin", length = 5)
    private String pauseDejeunerFin;

    @Column(name = "ouvert24h", nullable = false)
    private boolean ouvert24h = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_information_service", nullable = false,
            foreignKey = @ForeignKey(name = "fk_horaire_info_service"))
    private InformationService informationService;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_jour", nullable = false,
            foreignKey = @ForeignKey(name = "fk_horaire_jour"))
    private Jour jour;
}
