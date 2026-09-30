package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Ligne de l'espace « Mes favoris » : le service favori vu par son propriétaire (le client). */
@Data
public class FavoriDto implements Serializable {
    private Long id;
    private LocalDateTime dateAjout;

    private Long serviceId;
    private String serviceLibelle;
    private String serviceDescription;
    private String categorieLibelle;
    private String categorieIcone;
    private String villeLibelle;
    private boolean disponible;
    private double noteMoyenne;
    private long nombreAvis;
}
