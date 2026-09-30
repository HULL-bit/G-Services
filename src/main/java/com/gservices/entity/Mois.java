package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** {@code Mois} d'une {@link Annee} donnée (le nombre de jours dépend de l'année). */
@Entity
@Table(name = "mois", uniqueConstraints = @UniqueConstraint(
        name = "uk_mois_annee_numero", columnNames = {"id_annee", "numero_mois"}),
        indexes = @Index(name = "idx_mois_annee", columnList = "id_annee"))
@Getter
@Setter
public class Mois extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mois")
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "libelle", nullable = false, length = 20)
    private String libelle;

    @Size(max = 5)
    @Column(name = "abreviation", length = 5)
    private String abreviation;

    /** 1 = janvier … 12 = décembre. */
    @Column(name = "numero_mois", nullable = false)
    private int numeroMois;

    @Column(name = "nombre_jours", nullable = false)
    private int nombreJours;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_annee", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mois_annee"))
    private Annee annee;
}
