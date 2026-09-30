package com.gservices.service.impl;

import com.gservices.dto.RoleDto;
import com.gservices.entity.Personne;
import com.gservices.entity.Profil;
import com.gservices.entity.Role;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.RoleMapper;
import com.gservices.repository.PersonneRepository;
import com.gservices.repository.ProfilRepository;
import com.gservices.repository.RoleRepository;
import com.gservices.service.RoleService;
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
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PersonneRepository personneRepository;
    private final ProfilRepository profilRepository;
    private final RoleMapper mapper;

    public RoleServiceImpl(RoleRepository roleRepository,
                           PersonneRepository personneRepository,
                           ProfilRepository profilRepository,
                           RoleMapper mapper) {
        this.roleRepository = roleRepository;
        this.personneRepository = personneRepository;
        this.profilRepository = profilRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_LIRE)")
    public Page<RoleDto> rechercher(String filtre, Pageable pageable) {
        return roleRepository.findAll(filtreSpec(filtre), pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_LIRE)")
    public List<RoleDto> parPersonne(Long idPersonne) {
        return roleRepository.findByPersonneId(idPersonne).stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_LIRE)")
    public List<RoleDto> parProfil(Long idProfil) {
        return roleRepository.findByProfilId(idProfil).stream().map(mapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_AFFECTER)")
    public RoleDto attribuer(Long idPersonne, Long idProfil) {
        Role role = roleRepository.findByPersonneIdAndProfilId(idPersonne, idProfil)
                .orElseGet(Role::new);
        if (role.getId() == null) {
            Personne personne = personneRepository.findById(idPersonne)
                    .orElseThrow(() -> new ResourceNotFoundException(Personne.class, idPersonne));
            Profil profil = profilRepository.findById(idProfil)
                    .orElseThrow(() -> new ResourceNotFoundException(Profil.class, idProfil));
            role.setPersonne(personne);
            role.setProfil(profil);
            role.setDateAttribution(LocalDateTime.now());
        }
        role.setEtat(true);
        return mapper.toDto(roleRepository.save(role));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_AFFECTER)")
    public void basculerEtat(Long idRole) {
        Role role = roleRepository.findById(idRole)
                .orElseThrow(() -> new ResourceNotFoundException(Role.class, idRole));
        if (role.isEtat()
                && "superadmin".equalsIgnoreCase(role.getPersonne().getLogin())
                && "SUPER_ADMIN".equalsIgnoreCase(role.getProfil().getLibelle())) {
            throw new BusinessException("Le rôle SUPER_ADMIN du compte d'amorçage ne peut pas être retiré.");
        }
        role.setEtat(!role.isEtat());
    }

    private static Specification<Role> filtreSpec(String filtre) {
        if (!StringUtils.hasText(filtre)) {
            return null;
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("personne").get("nom")), motif),
                cb.like(cb.lower(root.get("personne").get("login")), motif),
                cb.like(cb.lower(root.get("profil").get("libelle")), motif));
    }
}
