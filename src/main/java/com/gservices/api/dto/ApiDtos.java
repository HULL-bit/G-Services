package com.gservices.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Contrats d'entrée / sortie de l'API REST mobile. */
public final class ApiDtos {

    private ApiDtos() {}

    public record LoginRequest(@NotBlank String login, @NotBlank String motDePasse) {}

    public record RegisterRequest(
            @NotBlank String typeCompte,        // CLIENT | FOURNISSEUR
            @NotBlank @Size(max = 80) String nom,
            @NotBlank @Size(max = 80) String prenom,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 3, max = 60) String login,
            @NotBlank @Size(min = 8, max = 72) String motDePasse,
            Long categorieSpecialiteId,
            String serviceLibelle) {}

    public record AuthResponse(String token, long expiresIn, UserInfo utilisateur) {}

    public record UserInfo(Long id, String login, String nomComplet, List<String> profils) {}

    public record AvisRequest(@Min(1) @Max(5) int note, @Size(max = 2000) String commentaire) {}

    public record LigneCommandeRequest(Long articleId, @Min(1) int quantite) {}

    public record CommandeRequest(
            List<LigneCommandeRequest> lignes,
            @Size(max = 255) String adresseLivraison,
            @Size(max = 30) String telephoneContact,
            @Size(max = 1000) String commentaire) {}

    public record SignalementRequest(
            @jakarta.validation.constraints.NotNull com.gservices.entity.MotifSignalement motif,
            @Size(max = 1000) String description) {}

    public record MessageResponse(String message) {}
}
