package com.gservices.dto;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/** Une section (branche) du menu d'administration = une {@code BranchePermission} + ses entrées visibles. */
@Getter
public class SectionMenuDto implements Serializable {

    private final String code;
    private final String libelle;
    private final String icone;
    private final List<EntreeMenuDto> entrees;

    public SectionMenuDto(String code, String libelle, String icone, List<EntreeMenuDto> entrees) {
        this.code = code;
        this.libelle = libelle;
        this.icone = icone;
        this.entrees = entrees;
    }

    public boolean isNonVide() {
        return entrees != null && !entrees.isEmpty();
    }
}
