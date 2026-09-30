package com.gservices.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AvisDto implements Serializable {
    private Long id;
    @Min(1) @Max(5) private int note;
    @Size(max = 2000) private String commentaire;
    @Size(max = 2000) private String reponsePrestataire;
    private boolean estModere;
    private LocalDateTime date;
    private boolean etat = true;

    private Long personneId;
    private String personneNom;

    private Long serviceId;
    private String serviceLibelle;
    private Long produitId;
    private String produitLibelle;
    private Long anneeId;
    private Integer anneeValeur;

    /** SERVICE ou PRODUIT. */
    private String cibleType;
    private String cibleLibelle;
}
