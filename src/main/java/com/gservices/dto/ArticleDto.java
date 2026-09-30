package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ArticleDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 60) private String reference;
    @Size(max = 1000) private String description;
    @NotNull private BigDecimal prix;
    @Size(max = 10) private String devise;
    @Size(max = 20) private String modeVente;
    @Size(max = 160) private String image;
    private boolean disponibilite = true;
    private boolean promotion = false;
    private BigDecimal tauxRemisePourcentage;
    private boolean etat = true;
    @NotNull private Long produitId;
    private String produitLibelle;
    private BigDecimal prixNet;
    private long nbVarietes;
}
