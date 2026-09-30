package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * {@code Avis} — <strong>classe d'association</strong> {@link Personne} ×
 * ({@link Service} ou {@link Produit}), rattachée à une {@link Annee}
 * (« publié en »). Un avis n'est visible publiquement qu'une fois modéré
 * ({@code estModere = true}) et actif ; la note est comprise entre 1 et 5.
 */
@Entity
@Table(name = "avis", indexes = {
        @Index(name = "idx_avis_personne", columnList = "id_personne"),
        @Index(name = "idx_avis_service", columnList = "id_service"),
        @Index(name = "idx_avis_produit", columnList = "id_produit"),
        @Index(name = "idx_avis_annee", columnList = "id_annee"),
        @Index(name = "idx_avis_modere", columnList = "est_modere")
})
@Getter
@Setter
public class Avis extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avis")
    private Long id;

    @Min(1)
    @Max(5)
    @Column(name = "note", nullable = false)
    private int note;

    @Size(max = 2000)
    @Column(name = "commentaire", length = 2000)
    private String commentaire;

    @Size(max = 2000)
    @Column(name = "reponse_prestataire", length = 2000)
    private String reponsePrestataire;

    @Column(name = "est_modere", nullable = false)
    private boolean estModere = false;

    @CreationTimestamp
    @Column(name = "date", nullable = false, updatable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_personne", nullable = false,
            foreignKey = @ForeignKey(name = "fk_avis_personne"))
    private Personne personne;

    /** Cible « service » — {@code null} si l'avis porte sur un produit. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_service", foreignKey = @ForeignKey(name = "fk_avis_service"))
    private Service service;

    /** Cible « produit » — {@code null} si l'avis porte sur un service. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", foreignKey = @ForeignKey(name = "fk_avis_produit"))
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_annee", nullable = false,
            foreignKey = @ForeignKey(name = "fk_avis_annee"))
    private Annee annee;

    /** {@code true} si l'avis est publiable (modéré + actif). */
    @Transient
    public boolean isPublic() {
        return estModere && isEtat();
    }
}
