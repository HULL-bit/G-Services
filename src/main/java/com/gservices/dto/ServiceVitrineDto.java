package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;

/** Carte « service » pour la vitrine publique. */
@Data
public class ServiceVitrineDto implements Serializable {
    private Long id;
    private String libelle;
    private String description;
    private String image;
    private Double prixIndicatif;
    private String categorieLibelle;
    private String categorieIcone;
    private String villeLibelle;
    private boolean disponible;
    private double noteMoyenne;
    private long nombreAvis;
    private int nbCatalogues;
    private Long proprietaireId;
    private Double latitude;
    private Double longitude;
    private String adresse;
    private boolean commandeADistance;
}
