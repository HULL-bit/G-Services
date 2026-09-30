package com.gservices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/** Compteurs affichés sur le tableau de bord d'administration. */
@Getter
@AllArgsConstructor
public class TableauBordDto implements Serializable {
    private final long personnes;
    private final long personnesActives;
    private final long personnesVerrouillees;
    private final long profils;
    private final long permissions;
    private final long branches;
}
