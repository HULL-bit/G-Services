package com.gservices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * {@code Role} — <strong>classe d'association datée</strong> du paquetage GestUser.
 *
 * <p>Réifie la relation n-n {@code Personne × Profil} (« accorde ») : quel profil
 * est accordé à quelle personne, depuis quand, et si l'affectation est active.</p>
 */
@Entity
@Table(name = "role", uniqueConstraints = @UniqueConstraint(
        name = "uk_role_personne_profil", columnNames = {"id_personne", "id_profil"}),
        indexes = {
                @Index(name = "idx_role_personne", columnList = "id_personne"),
                @Index(name = "idx_role_profil", columnList = "id_profil")
        })
@Getter
@Setter
public class Role extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_personne", nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_personne"))
    private Personne personne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_profil", nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_profil"))
    private Profil profil;

    /** Date d'attribution du profil à la personne. */
    @Column(name = "date_attribution", nullable = false)
    private LocalDateTime dateAttribution = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;
}
