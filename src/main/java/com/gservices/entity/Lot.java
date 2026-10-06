package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** {@code Lot} d'entrée en {@link Stock}, reçu au cours d'un {@link Mois} donné. */
@Entity
@Table(name = "lot", indexes = {
        @Index(name = "idx_lot_stock", columnList = "id_stock"),
        @Index(name = "idx_lot_mois", columnList = "id_mois"),
        @Index(name = "idx_lot_numero", columnList = "numero_lot")
})
@Getter
@Setter
public class Lot extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lot")
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(name = "numero_lot", nullable = false, length = 60)
    private String numeroLot;

    @Column(name = "quantite", nullable = false)
    private int quantite = 0;

    @Column(name = "prix_achat", precision = 12, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "date_entree")
    private LocalDate dateEntree;

    @Column(name = "date_peremption")
    private LocalDate datePeremption;

    /** Horodatage système de l'enregistrement du lot (distinct de {@link #dateEntree}, choisie par l'utilisateur). */
    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_stock", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lot_stock"))
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_mois", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lot_mois"))
    private Mois mois;

    /** {@code true} si la date de péremption est dépassée. */
    @Transient
    public boolean isPerime() {
        return datePeremption != null && datePeremption.isBefore(LocalDate.now());
    }
}
