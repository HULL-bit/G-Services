package com.gservices.service;

import com.gservices.dto.ProfilDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface ProfilService {

    Page<ProfilDto> rechercher(String filtre, Pageable pageable);

    List<ProfilDto> tousActifs();

    ProfilDto parId(Long id);

    ProfilDto creer(ProfilDto dto);

    ProfilDto modifier(Long id, ProfilDto dto);

    void basculerEtat(Long id);

    /** Réconcilie les permissions du profil (classe d'association {@code RoleProfil}). */
    ProfilDto affecterPermissions(Long idProfil, Set<Long> permissionIds);

    /** Réconcilie les branches de menu visibles pour le profil. */
    ProfilDto affecterBranches(Long idProfil, Set<Long> brancheIds);
}
