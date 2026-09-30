package com.gservices.dto;

import com.gservices.entity.Sexe;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO d'échange pour {@link com.gservices.entity.Personne}.
 * Le mot de passe n'est jamais exposé en lecture ; il n'est renseigné que pour
 * une création ou une réinitialisation explicite.
 */
@Data
public class PersonneDto implements Serializable {

    private Long id;

    @NotBlank @Size(max = 80)
    private String nom;

    @NotBlank @Size(max = 80)
    private String prenom;

    private Sexe sexe;
    private LocalDate dateNaissance;

    @Size(max = 120) private String lieuNaissance;
    @Size(max = 80)  private String nationalite;
    @Size(max = 255) private String adresse;
    @Size(max = 20)  private String codePostal;
    @Size(max = 30)  private String telephone1;
    @Size(max = 30)  private String telephone2;
    @Size(max = 30)  private String telephone3;

    @Email @Size(max = 150) private String email1;
    @Email @Size(max = 150) private String email2;

    @NotBlank @Size(max = 60)
    private String login;

    /** Mot de passe en clair — uniquement en écriture (création / réinitialisation). */
    @Size(min = 8, max = 72)
    private String nouveauMotDePasse;

    @Size(max = 255) private String photoProfil;

    private LocalDateTime dateInscription;
    private LocalDateTime derniereConnexion;
    private boolean compteVerrouille;
    private boolean etat = true;
    /** Compte validé par un administrateur (sinon : en attente, non connectable). */
    private boolean valide = true;
    /** Libellé du service géré par ce fournisseur (lecture seule, écrans dédiés). */
    private String serviceGereLibelle;

    /**
     * Spécialité métier du fournisseur : la seule catégorie de services dans
     * laquelle ce prestataire peut être propriétaire et gérer une offre.
     */
    private Long categorieSpecialiteId;
    private String categorieSpecialiteLibelle;
    /** {@code true} si ce fournisseur ne gère encore aucun service (règle « 1 fournisseur = 1 service »). */
    private boolean disponible = true;

    /** Libellés des profils actifs (lecture seule). */
    private List<String> profils;

    public String getNomComplet() {
        return (prenom == null ? "" : prenom + " ") + (nom == null ? "" : nom);
    }
}
