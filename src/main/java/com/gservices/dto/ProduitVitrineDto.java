package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProduitVitrineDto implements Serializable {
    private Long id;
    private String libelle;
    private String description;
    private String image;
    private double noteMoyenne;
    private long nombreAvis;
    private List<ArticleVitrineDto> articles = new ArrayList<>();

    @Data
    public static class ArticleVitrineDto implements Serializable {
        private Long id;
        private String reference;
        private String description;
        private BigDecimal prix;
        private BigDecimal prixNet;
        private String devise;
        private boolean promotion;
        private BigDecimal tauxRemisePourcentage;
        private boolean disponibilite;
    }
}
