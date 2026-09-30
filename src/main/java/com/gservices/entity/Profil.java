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
 * {@code Profil} — paquetage <strong>GestUser</strong>.
 *
 * <p>Ensemble cohérent de rôles applicatifs. Ses permissions sont portées par la
 * classe d'association datée {@link RoleProfil} ({@code Profil × Permission}).
 * Un profil est par ailleurs rattaché à des {@link BranchePermission}
 * (regroupements servant à générer le menu d'administration).</p>
 */
@Entity
@Table(name = "profil", indexes = {
        @Index(name = "idx_profil_libelle", columnList = "libelle", unique = true)
})
@Getter
@Setter
public class Profil extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profil")
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "libelle", nullable = false, unique = true, length = 80)
    private String libelle;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    /** Niveau hiérarchique (0 = super-admin, valeurs croissantes = moins de droits). */
    @Column(name = "niveau", nullable = false)
    private int niveau = 100;

    @CreationTimestamp
    @Column(name = "date", nullable = false, updatable = false)
    private LocalDateTime date;

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;

    /** Permissions accordées au profil (classe d'association datée {@link RoleProfil}). */
    @OneToMany(mappedBy = "profil", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoleProfil> roleProfils = new LinkedHashSet<>();

    /** Personnes portant ce profil (classe d'association datée {@link Role}). */
    @OneToMany(mappedBy = "profil")
    private Set<Role> roles = new LinkedHashSet<>();

    /** Branches (sections de menu) visibles pour ce profil. */
    @ManyToMany
    @JoinTable(name = "profil_branche_permission",
            joinColumns = @JoinColumn(name = "id_profil",
                    foreignKey = @ForeignKey(name = "fk_pbp_profil")),
            inverseJoinColumns = @JoinColumn(name = "id_branche_permission",
                    foreignKey = @ForeignKey(name = "fk_pbp_branche")))
    private Set<BranchePermission> branchePermissions = new LinkedHashSet<>();

    public void addRoleProfil(RoleProfil rp) {
        roleProfils.add(rp);
        rp.setProfil(this);
    }

    public void removeRoleProfil(RoleProfil rp) {
        roleProfils.remove(rp);
        rp.setProfil(null);
    }
}
