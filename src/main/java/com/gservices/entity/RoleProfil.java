package com.gservices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * {@code RoleProfil} — <strong>classe d'association datée</strong> du paquetage GestUser.
 *
 * <p>Réifie la relation n-n {@code Profil × Permission} : quelle permission est
 * affectée à quel profil, depuis quand, et si l'affectation est active.</p>
 */
@Entity
@Table(name = "role_profil", uniqueConstraints = @UniqueConstraint(
        name = "uk_role_profil_profil_permission", columnNames = {"id_profil", "id_permission"}),
        indexes = {
                @Index(name = "idx_role_profil_profil", columnList = "id_profil"),
                @Index(name = "idx_role_profil_permission", columnList = "id_permission")
        })
@Getter
@Setter
public class RoleProfil extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role_profil")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_profil", nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_profil_profil"))
    private Profil profil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_permission", nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_profil_permission"))
    private Permission permission;

    /** Date d'affectation de la permission au profil. */
    @Column(name = "date_affectation", nullable = false)
    private LocalDateTime dateAffectation = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;
}
