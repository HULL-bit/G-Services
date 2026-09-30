package com.gservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * Création d'un compte depuis le back-office (écrans « Fournisseurs » / « Clients »).
 * Le compte est <strong>validé d'emblée</strong> puisque créé par un administrateur.
 */
@Data
public class CreationCompteDto implements Serializable {

    @NotBlank @Size(max = 80) private String nom;
    @NotBlank @Size(max = 80) private String prenom;
    @NotBlank @Size(max = 60) private String login;
    @Email @Size(max = 150) private String email;
    @Size(max = 30) private String telephone;

    @NotBlank @Size(min = 8, max = 72) private String motDePasse;

    /** Obligatoire pour un fournisseur : sa catégorie de spécialité. */
    private Long categorieSpecialiteId;

    /** Optionnel : libellé du service à créer pour ce fournisseur (validé d'emblée). */
    @Size(max = 120) private String serviceLibelle;
}
