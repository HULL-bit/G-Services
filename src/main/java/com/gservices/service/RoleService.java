package com.gservices.service;

import com.gservices.dto.RoleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Gestion de la classe d'association {@link com.gservices.entity.Role} (Personne × Profil). */
public interface RoleService {

    Page<RoleDto> rechercher(String filtre, Pageable pageable);

    List<RoleDto> parPersonne(Long idPersonne);

    List<RoleDto> parProfil(Long idProfil);

    /** Accorde un profil à une personne (ou réactive l'affectation existante). */
    RoleDto attribuer(Long idPersonne, Long idProfil);

    void basculerEtat(Long idRole);
}
