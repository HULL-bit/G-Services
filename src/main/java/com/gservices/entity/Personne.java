package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code Personne} — paquetage <strong>GestUser</strong>.
 *
 * <p>Alimente Spring Security ({@code login} / {@code password} BCrypt / {@code etat}).
 * Ses profils applicatifs sont portés par la classe d'association {@link Role}
 * (relation datée {@code Personne × Profil}).</p>
 *
 * <p>Les colonnes {@code tentatives_echouees} et {@code verrouille_jusqua} ne
 * figurent pas dans le diagramme : elles matérialisent la règle de gestion
 * « verrouillage du compte après N échecs de connexion » (cf. §5 du cahier des
 * charges).</p>
 */
@Entity
@Table(name = "personne", indexes = {
        @Index(name = "idx_personne_login", columnList = "login", unique = true),
        @Index(name = "idx_personne_email1", columnList = "email1"),
        @Index(name = "idx_personne_nom", columnList = "nom")
})
@Getter
@Setter
public class Personne extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personne")
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nom", nullable = false, length = 80)
    private String nom;

    @NotBlank
    @Size(max = 80)
    @Column(name = "prenom", nullable = false, length = 80)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexe", length = 10)
    private Sexe sexe;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Size(max = 120)
    @Column(name = "lieu_naissance", length = 120)
    private String lieuNaissance;

    @Size(max = 80)
    @Column(name = "nationalite", length = 80)
    private String nationalite;

    @Size(max = 255)
    @Column(name = "adresse")
    private String adresse;

    @Size(max = 20)
    @Column(name = "code_postal", length = 20)
    private String codePostal;

    @Size(max = 30)
    @Column(name = "telephone1", length = 30)
    private String telephone1;

    @Size(max = 30)
    @Column(name = "telephone2", length = 30)
    private String telephone2;

    @Size(max = 30)
    @Column(name = "telephone3", length = 30)
    private String telephone3;

    @Email
    @Size(max = 150)
    @Column(name = "email1", length = 150)
    private String email1;

    @Email
    @Size(max = 150)
    @Column(name = "email2", length = 150)
    private String email2;

    @NotBlank
    @Size(max = 60)
    @Column(name = "login", nullable = false, unique = true, length = 60)
    private String login;

    /** Hash BCrypt — jamais le mot de passe en clair. */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Size(max = 255)
    @Column(name = "photo_profil")
    private String photoProfil;

    @CreationTimestamp
    @Column(name = "date_inscription", nullable = false, updatable = false)
    private LocalDateTime dateInscription;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    /**
     * Validation du compte par un administrateur (hors UML). Un compte auto-inscrit
     * (client ou fournisseur) reste {@code false} — donc non connectable — jusqu'à
     * ce qu'un administrateur le valide. Les comptes créés depuis le back-office
     * sont {@code true} d'emblée.
     */
    @Column(name = "valide", nullable = false)
    private boolean valide = true;

    // --- Sécurité : verrouillage après N échecs (hors UML, cf. javadoc) ---
    @Column(name = "tentatives_echouees", nullable = false)
    private int tentativesEchouees = 0;

    @Column(name = "verrouille_jusqua")
    private LocalDateTime verrouilleJusqua;

    /**
     * Spécialité métier du fournisseur de services (hors UML — règle de gestion).
     * Un prestataire n'exerce que dans <strong>une seule</strong> catégorie de
     * services : il ne peut être désigné propriétaire que de services de cette
     * catégorie. {@code null} pour les personnes qui ne sont pas prestataires.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie_specialite",
            foreignKey = @ForeignKey(name = "fk_personne_categorie_specialite"))
    private CategorieService categorieSpecialite;

    /** Profils accordés à cette personne (classe d'association datée {@link Role}). */
    @OneToMany(mappedBy = "personne", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Role> roles = new LinkedHashSet<>();

    /** Surcharges de permission propres à cette personne (hors UML — cf. {@link PersonnePermission}). */
    @OneToMany(mappedBy = "personne", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersonnePermission> permissionsDirectes = new LinkedHashSet<>();

    // ------------------------------------------------------------------ util

    public String getNomComplet() {
        return (prenom == null ? "" : prenom + " ") + (nom == null ? "" : nom);
    }

    /** {@code true} si le compte est actuellement verrouillé (échecs de connexion). */
    @Transient
    public boolean isCompteVerrouille() {
        return verrouilleJusqua != null && verrouilleJusqua.isAfter(LocalDateTime.now());
    }

    public void addRole(Role role) {
        roles.add(role);
        role.setPersonne(this);
    }

    public void removeRole(Role role) {
        roles.remove(role);
        role.setPersonne(null);
    }

    public void addPermissionDirecte(PersonnePermission pp) {
        permissionsDirectes.add(pp);
        pp.setPersonne(this);
    }
}
