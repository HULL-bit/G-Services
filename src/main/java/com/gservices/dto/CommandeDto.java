package com.gservices.dto;

import com.gservices.entity.StatutCommande;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO d'échange pour {@link com.gservices.entity.Commande} — utilisé aussi bien
 * pour le tunnel de commande public que pour l'écran d'administration.
 */
@Data
public class CommandeDto implements Serializable {

    private Long id;
    private String reference;
    private LocalDateTime dateCommande;
    private StatutCommande statut;

    private Long serviceId;
    private String serviceLibelle;
    private String categorieLibelle;

    private Long clientId;
    private String clientNom;

    private String proprietaireNom;

    @Size(max = 255) private String adresseLivraison;
    @Size(max = 30)  private String telephoneContact;
    @Size(max = 1000) private String commentaire;

    private BigDecimal montantTotal;
    private int nombreArticles;

    private List<LigneCommandeDto> lignes = new ArrayList<>();
}
