package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code Permission} — paquetage <strong>GestUser</strong>.
 *
 * <p>Autorité fine du RBAC : {@code code} (identifiant unique utilisé dans
 * {@code @PreAuthorize("hasAuthority('...')")} et pour masquer l'IHM),
 * {@code actionAutorisee} (LIRE / CREER / MODIFIER / SUPPRIMER…), {@code niveau}.
 * Rattachée à une {@link BranchePermission} pour le menu.</p>
 */
@Entity
@Table(name = "permission", indexes = {
        @Index(name = "idx_permission_code", columnList = "code", unique = true),
        @Index(name = "idx_permission_branche", columnList = "id_branche_permission")
})
@Getter
@Setter
public class Permission extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permission")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @NotBlank
    @Size(max = 120)
    @Column(name = "libelle", nullable = false, length = 120)
    private String libelle;

    /** Action couverte : LIRE, CREER, MODIFIER, SUPPRIMER, EXPORTER, ADMINISTRER… */
    @Size(max = 40)
    @Column(name = "action_autorisee", length = 40)
    private String actionAutorisee;

    /** Référence de la vue/écran cible (pour construire l'entrée de menu). */
    @Size(max = 150)
    @Column(name = "reference", length = 150)
    private String reference;

    @Column(name = "niveau", nullable = false)
    private int niveau = 0;

    @CreationTimestamp
    @Column(name = "date", nullable = false, updatable = false)
    private LocalDateTime date;

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_branche_permission",
            foreignKey = @ForeignKey(name = "fk_permission_branche"))
    private BranchePermission branchePermission;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoleProfil> roleProfils = new LinkedHashSet<>();
}
