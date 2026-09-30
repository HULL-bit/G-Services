package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class LigneCommandeDto implements Serializable {
    private Long id;
    private Long articleId;
    private String articleReference;
    private String articleDescription;
    private int quantite = 1;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;
    private boolean etat = true;
}
