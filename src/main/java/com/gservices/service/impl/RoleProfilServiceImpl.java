package com.gservices.service.impl;

import com.gservices.dto.RoleProfilDto;
import com.gservices.entity.Permission;
import com.gservices.entity.Profil;
import com.gservices.entity.RoleProfil;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.RoleProfilMapper;
import com.gservices.repository.PermissionRepository;
import com.gservices.repository.ProfilRepository;
import com.gservices.repository.RoleProfilRepository;
import com.gservices.service.RoleProfilService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RoleProfilServiceImpl implements RoleProfilService {

    private final RoleProfilRepository roleProfilRepository;
    private final ProfilRepository profilRepository;
    private final PermissionRepository permissionRepository;
    private final RoleProfilMapper mapper;

    public RoleProfilServiceImpl(RoleProfilRepository roleProfilRepository,
                                 ProfilRepository profilRepository,
                                 PermissionRepository permissionRepository,
                                 RoleProfilMapper mapper) {
        this.roleProfilRepository = roleProfilRepository;
        this.profilRepository = profilRepository;
        this.permissionRepository = permissionRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_LIRE)")
    public Page<RoleProfilDto> rechercher(String filtre, Pageable pageable) {
        return roleProfilRepository.findAll(filtreSpec(filtre), pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_LIRE)")
    public List<RoleProfilDto> parProfil(Long idProfil) {
        return roleProfilRepository.findByProfilId(idProfil).stream().map(mapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_AFFECTER)")
    public RoleProfilDto affecter(Long idProfil, Long idPermission) {
        RoleProfil rp = roleProfilRepository.findByProfilIdAndPermissionId(idProfil, idPermission)
                .orElseGet(RoleProfil::new);
        if (rp.getId() == null) {
            Profil profil = profilRepository.findById(idProfil)
                    .orElseThrow(() -> new ResourceNotFoundException(Profil.class, idProfil));
            Permission permission = permissionRepository.findById(idPermission)
                    .orElseThrow(() -> new ResourceNotFoundException(Permission.class, idPermission));
            rp.setProfil(profil);
            rp.setPermission(permission);
            rp.setDateAffectation(LocalDateTime.now());
        }
        rp.setEtat(true);
        return mapper.toDto(roleProfilRepository.save(rp));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_AFFECTER)")
    public void basculerEtat(Long idRoleProfil) {
        RoleProfil rp = roleProfilRepository.findById(idRoleProfil)
                .orElseThrow(() -> new ResourceNotFoundException(RoleProfil.class, idRoleProfil));
        rp.setEtat(!rp.isEtat());
    }

    private static Specification<RoleProfil> filtreSpec(String filtre) {
        if (!StringUtils.hasText(filtre)) {
            return null;
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("profil").get("libelle")), motif),
                cb.like(cb.lower(root.get("permission").get("code")), motif));
    }
}
