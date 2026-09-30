package com.gservices.service;

import com.gservices.dto.SectionMenuDto;

import java.util.List;

/** Construit le menu d'administration à partir des {@code BranchePermission} / {@code Permission}. */
public interface MenuService {

    /**
     * Menu filtré pour l'utilisateur authentifié courant : seules les branches
     * actives ayant au moins une permission de lecture ({@code *_LIRE}) détenue
     * par l'utilisateur sont retournées.
     */
    List<SectionMenuDto> menuCourant();
}
