package com.gservices.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class StockDto implements Serializable {
    private Long id;
    @PositiveOrZero private int quantiteTotale;
    @PositiveOrZero private int seuilAlerte;
    private boolean etat = true;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    @NotNull private Long articleId;
    private String articleReference;
    private String produitLibelle;
    /** Chaîne Article → Produit → Catalogue → Service : contexte affiché à l'admin. */
    private String serviceLibelle;
    private String categorieLibelle;
    private String proprietaireNom;
    private int quantiteDisponible;
    private boolean enAlerte;
    private long nbLots;
    private long nbLotsPerimes;
}
