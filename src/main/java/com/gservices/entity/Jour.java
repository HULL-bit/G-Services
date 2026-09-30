package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** {@code Jour} de la semaine — référentiel (lundi … dimanche). */
@Entity
@Table(name = "jour", indexes = {
        @Index(name = "idx_jour_numero", columnList = "numero_jour_semaine", unique = true)
})
@Getter
@Setter
public class Jour extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jour")
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "libelle", nullable = false, length = 20)
    private String libelle;

    @Size(max = 5)
    @Column(name = "abreviation", length = 5)
    private String abreviation;

    /** 1 = lundi … 7 = dimanche (ISO-8601). */
    @Column(name = "numero_jour_semaine", nullable = false, unique = true)
    private int numeroJourSemaine;

    @Column(name = "est_weekend", nullable = false)
    private boolean estWeekend = false;

    @Column(name = "est_ferie", nullable = false)
    private boolean estFerie = false;
}
