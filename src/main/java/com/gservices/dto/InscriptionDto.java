package com.gservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * Formulaire d'auto-inscription d'un visiteur. Il choisit d'être
 * <strong>client</strong> (dépose des avis, passe des commandes) ou
 * <strong>fournisseur</strong> (gère un service). Dans les deux cas le compte
 * reste en attente de validation par un administrateur.
 */
@Data
public class InscriptionDto implements Serializable {

    /** {@code CLIENT} (défaut) ou {@code FOURNISSEUR}. */
    private String typeCompte = "CLIENT";

    @NotBlank @Size(max = 80) private String nom;
    @NotBlank @Size(max = 80) private String prenom;
    @NotBlank @Email @Size(max = 150) private String email;
    @NotBlank @Size(min = 3, max = 60) private String login;
    @NotBlank @Size(min = 8, max = 72) private String motDePasse;
    private String motDePasseConfirme;

    // --- fournisseur uniquement ---
    /** Catégorie de spécialité (obligatoire si {@code typeCompte = FOURNISSEUR}). */
    private Long categorieSpecialiteId;
    /** Nom du service que le fournisseur souhaite ouvrir. */
    @Size(max = 120) private String serviceLibelle;

    public boolean isFournisseur() {
        return "FOURNISSEUR".equalsIgnoreCase(typeCompte);
    }
}
