package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/** {@code Annee} calendaire (2024, 2025 …) — contient ses 12 {@link Mois}. */
@Entity
@Table(name = "annee", indexes = {
        @Index(name = "idx_annee_valeur", columnList = "valeur_annee", unique = true)
})
@Getter
@Setter
public class Annee extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_annee")
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "libelle", nullable = false, length = 20)
    private String libelle;

    @Column(name = "valeur_annee", nullable = false, unique = true)
    private int valeurAnnee;

    @Column(name = "est_bissextile", nullable = false)
    private boolean estBissextile = false;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @OneToMany(mappedBy = "annee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Mois> mois = new LinkedHashSet<>();

    public void addMois(Mois m) {
        mois.add(m);
        m.setAnnee(this);
    }
}
