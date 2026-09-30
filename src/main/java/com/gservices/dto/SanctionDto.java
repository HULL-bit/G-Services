package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Ligne de la liste « Services bloqués » (back-office). */
@Data
public class SanctionDto implements Serializable {
    private Long id;
    private String motif;
    private LocalDateTime dateSanction;
    private LocalDateTime dateLevee;

    private Long serviceId;
    private String serviceLibelle;
    private String categorieLibelle;

    private String adminNom;

    public boolean isActive() {
        return dateLevee == null;
    }
}
