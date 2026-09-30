package com.gservices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * {@code Favori} — <strong>classe d'association</strong> {@link Personne} ×
 * {@link Service} (hors UML, cf. cahier « espaces favoris »). Un client
 * ajoute/retire un service de son espace favoris ; {@code etat} porte le
 * bascule (toggle) plutôt qu'une suppression physique, pour garder
 * l'historique de la première mise en favori.
 */
@Entity
@Table(name = "favori",
        uniqueConstraints = @UniqueConstraint(name = "uk_favori_personne_service", columnNames = {"id_personne", "id_service"}),
        indexes = {
                @Index(name = "idx_favori_personne", columnList = "id_personne"),
                @Index(name = "idx_favori_service", columnList = "id_service")
        })
@Getter
@Setter
public class Favori extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favori")
    private Long id;

    @Column(name = "date_ajout", nullable = false)
    private LocalDateTime dateAjout = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_personne", nullable = false, foreignKey = @ForeignKey(name = "fk_favori_personne"))
    private Personne personne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false, foreignKey = @ForeignKey(name = "fk_favori_service"))
    private Service service;
}
