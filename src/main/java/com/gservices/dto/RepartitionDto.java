package com.gservices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/** Une barre de répartition du tableau de bord (libellé + valeur + % relatif au max). */
@Getter
@AllArgsConstructor
public class RepartitionDto implements Serializable {
    private final String libelle;
    private final long valeur;
    private final int pourcent;
}
