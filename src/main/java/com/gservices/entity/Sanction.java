package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * {@code Sanction} — hors UML, journal d'audit du blocage d'un {@link Service}
 * signalé (§3 « Système de sanction »). Tant que {@code dateLevee} est
 * {@code null}, la sanction est active et le service reste {@link Service#isBloque() bloqué}.
 */
@Entity
@Table(name = "sanction", indexes = @Index(name = "idx_sanction_service", columnList = "id_service"))
@Getter
@Setter
public class Sanction extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sanction")
    private Long id;

    @NotBlank
    @Size(max = 500)
    @Column(name = "motif", nullable = false, length = 500)
    private String motif;

    @Column(name = "date_sanction", nullable = false)
    private LocalDateTime dateSanction = LocalDateTime.now();

    /** {@code null} = sanction toujours active (service bloqué). */
    @Column(name = "date_levee")
    private LocalDateTime dateLevee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false, foreignKey = @ForeignKey(name = "fk_sanction_service"))
    private Service service;

    /** L'administrateur qui a prononcé la sanction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin", foreignKey = @ForeignKey(name = "fk_sanction_admin"))
    private Personne admin;

    @Transient
    public boolean isActive() {
        return dateLevee == null;
    }
}
