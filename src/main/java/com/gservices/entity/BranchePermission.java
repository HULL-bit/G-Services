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
 * {@code BranchePermission} — paquetage <strong>GestUser</strong>.
 *
 * <p>Regroupement de {@link Permission}s. Sert de <strong>source du menu
 * d'administration dynamique</strong> : chaque branche = une section de menu
 * ({@code libelle}, {@code icone}, {@code niveau} pour l'ordre), chaque
 * permission rattachée = une entrée de menu, visible si l'utilisateur courant
 * détient la permission.</p>
 */
@Entity
@Table(name = "branche_permission", indexes = {
        @Index(name = "idx_branche_code", columnList = "code", unique = true)
})
@Getter
@Setter
public class BranchePermission extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_branche_permission")
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(name = "code", nullable = false, unique = true, length = 60)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    /** Classe d'icône (PrimeIcons, ex. {@code pi pi-users}). */
    @Size(max = 60)
    @Column(name = "icone", length = 60)
    private String icone;

    /** Référence de navigation (chemin de la vue racine de la section, optionnel). */
    @Size(max = 150)
    @Column(name = "reference", length = 150)
    private String reference;

    /** Ordre d'affichage dans le menu. */
    @Column(name = "niveau", nullable = false)
    private int niveau = 0;

    @CreationTimestamp
    @Column(name = "date", nullable = false, updatable = false)
    private LocalDateTime date;

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;

    /** Permissions classées dans cette branche. */
    @OneToMany(mappedBy = "branchePermission")
    private Set<Permission> permissions = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "branchePermissions")
    private Set<Profil> profils = new LinkedHashSet<>();
}
