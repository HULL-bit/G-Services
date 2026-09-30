package com.gservices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/** Une entrée cliquable du menu d'administration (issue d'une {@code Permission} de lecture). */
@Getter
@AllArgsConstructor
public class EntreeMenuDto implements Serializable {
    private final String libelle;
    private final String icone;
    private final String url;
    private final String permissionCode;
}
