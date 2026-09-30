package com.gservices.dto;

import com.gservices.entity.MotifSignalement;
import com.gservices.entity.StatutSignalement;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Ligne de la file de modération « Signalements » (back-office). */
@Data
public class SignalementDto implements Serializable {
    private Long id;
    private MotifSignalement motif;
    private String description;
    private LocalDateTime date;
    private StatutSignalement statut;

    private Long personneId;
    private String personneNom;

    private Long serviceId;
    private String serviceLibelle;
    private String categorieLibelle;
    private boolean serviceBloque;
}
