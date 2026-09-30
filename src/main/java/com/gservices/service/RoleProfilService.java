package com.gservices.service;

import com.gservices.dto.RoleProfilDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Gestion de la classe d'association {@link com.gservices.entity.RoleProfil} (Profil × Permission). */
public interface RoleProfilService {

    Page<RoleProfilDto> rechercher(String filtre, Pageable pageable);

    List<RoleProfilDto> parProfil(Long idProfil);

    /** Affecte une permission à un profil (ou réactive l'affectation existante). */
    RoleProfilDto affecter(Long idProfil, Long idPermission);

    void basculerEtat(Long idRoleProfil);
}
