package com.gservices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * <strong>Hors UML</strong> — surcharge de permission au niveau d'un utilisateur,
 * appliquée <em>par-dessus</em> les permissions héritées de ses profils.
 *
 * <ul>
 *   <li>{@code accordee = true}  : la permission est <strong>ajoutée</strong> à la
 *       personne même si aucun de ses profils ne la porte ;</li>
 *   <li>{@code accordee = false} : la permission est <strong>retirée</strong> à la
 *       personne même si un de ses profils la porte.</li>
 * </ul>
 *
 * <p>Permet de cocher / décocher finement les permissions effectives d'un
 * utilisateur sans toucher aux profils partagés.</p>
 */
@Entity
@Table(name = "personne_permission", uniqueConstraints = @UniqueConstraint(
        name = "uk_personne_permission", columnNames = {"id_personne", "id_permission"}),
        indexes = {
                @Index(name = "idx_pp_personne", columnList = "id_personne"),
                @Index(name = "idx_pp_permission", columnList = "id_permission")
        })
@Getter
@Setter
public class PersonnePermission extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personne_permission")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_personne", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pp_personne"))
    private Personne personne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_permission", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pp_permission"))
    private Permission permission;

    /** {@code true} = ajoutée à l'utilisateur ; {@code false} = retirée à l'utilisateur. */
    @Column(name = "accordee", nullable = false)
    private boolean accordee = true;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;
}
