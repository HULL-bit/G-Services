package com.gservices.service.impl;

import com.gservices.dto.ProfilDto;
import com.gservices.entity.BranchePermission;
import com.gservices.entity.Permission;
import com.gservices.entity.Profil;
import com.gservices.entity.RoleProfil;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.ProfilMapper;
import com.gservices.repository.BranchePermissionRepository;
import com.gservices.repository.PermissionRepository;
import com.gservices.repository.ProfilRepository;
import com.gservices.repository.RoleProfilRepository;
import com.gservices.service.ProfilService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ProfilServiceImpl implements ProfilService {

    private final ProfilRepository repository;
    private final PermissionRepository permissionRepository;
    private final BranchePermissionRepository brancheRepository;
    private final RoleProfilRepository roleProfilRepository;
    private final ProfilMapper mapper;

    public ProfilServiceImpl(ProfilRepository repository,
                             PermissionRepository permissionRepository,
                             BranchePermissionRepository brancheRepository,
                             RoleProfilRepository roleProfilRepository,
                             ProfilMapper mapper) {
        this.repository = repository;
        this.permissionRepository = permissionRepository;
        this.brancheRepository = brancheRepository;
        this.roleProfilRepository = roleProfilRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_LIRE)")
    public Page<ProfilDto> rechercher(String filtre, Pageable pageable) {
        return repository.findAll(filtreSpec(filtre), pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_LIRE)")
    public List<ProfilDto> tousActifs() {
        return repository.findAll((r, q, cb) -> cb.isTrue(r.get("etat")), Sort.by("niveau"))
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_LIRE)")
    public ProfilDto parId(Long id) {
        return mapper.toDto(chargerDetail(id));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_CREER)")
    public ProfilDto creer(ProfilDto dto) {
        if (repository.existsByLibelleIgnoreCase(dto.getLibelle())) {
            throw new BusinessException("Le profil « " + dto.getLibelle() + " » existe déjà.");
        }
        Profil profil = repository.save(mapper.toEntity(dto));
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            reconcilierPermissions(profil, dto.getPermissionIds());
        }
        if (dto.getBrancheIds() != null && !dto.getBrancheIds().isEmpty()) {
            reconcilierBranches(profil, dto.getBrancheIds());
        }
        return mapper.toDto(profil);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_MODIFIER)")
    public ProfilDto modifier(Long id, ProfilDto dto) {
        Profil profil = chargerDetail(id);
        repository.findByLibelleIgnoreCase(dto.getLibelle())
                .filter(autre -> !autre.getId().equals(id))
                .ifPresent(autre -> {
                    throw new BusinessException("Le libellé « " + dto.getLibelle() + " » est déjà utilisé.");
                });
        mapper.update(profil, dto);
        return mapper.toDto(profil);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_SUPPRIMER)")
    public void basculerEtat(Long id) {
        Profil profil = chargerDetail(id);
        if (profil.isEtat() && "SUPER_ADMIN".equalsIgnoreCase(profil.getLibelle())) {
            throw new BusinessException("Le profil SUPER_ADMIN ne peut pas être désactivé.");
        }
        profil.setEtat(!profil.isEtat());
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_AFFECTER)")
    public ProfilDto affecterPermissions(Long idProfil, Set<Long> permissionIds) {
        Profil profil = chargerDetail(idProfil);
        reconcilierPermissions(profil, permissionIds == null ? Set.of() : permissionIds);
        return mapper.toDto(profil);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PROFIL_AFFECTER)")
    public ProfilDto affecterBranches(Long idProfil, Set<Long> brancheIds) {
        Profil profil = chargerDetail(idProfil);
        reconcilierBranches(profil, brancheIds == null ? Set.of() : brancheIds);
        return mapper.toDto(profil);
    }

    // ------------------------------------------------------------------ util

    private Profil chargerDetail(Long id) {
        return repository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Profil.class, id));
    }

    /** Ajoute les nouvelles permissions, retire celles décochées (via {@code RoleProfil}). */
    private void reconcilierPermissions(Profil profil, Set<Long> cibles) {
        Set<Long> actuelles = new HashSet<>();
        profil.getRoleProfils().removeIf(rp -> {
            boolean garder = cibles.contains(rp.getPermission().getId());
            if (garder) {
                actuelles.add(rp.getPermission().getId());
            }
            return !garder;
        });
        for (Long idPerm : cibles) {
            if (actuelles.contains(idPerm)) {
                continue;
            }
            Permission perm = permissionRepository.findById(idPerm)
                    .orElseThrow(() -> new ResourceNotFoundException(Permission.class, idPerm));
            RoleProfil rp = new RoleProfil();
            rp.setPermission(perm);
            rp.setEtat(true);
            profil.addRoleProfil(rp);
        }
    }

    private void reconcilierBranches(Profil profil, Set<Long> cibles) {
        profil.getBranchePermissions().removeIf(b -> !cibles.contains(b.getId()));
        Set<Long> actuelles = new HashSet<>();
        profil.getBranchePermissions().forEach(b -> actuelles.add(b.getId()));
        for (Long idBranche : cibles) {
            if (actuelles.contains(idBranche)) {
                continue;
            }
            BranchePermission b = brancheRepository.findById(idBranche)
                    .orElseThrow(() -> new ResourceNotFoundException(BranchePermission.class, idBranche));
            profil.getBranchePermissions().add(b);
        }
    }

    private static Specification<Profil> filtreSpec(String filtre) {
        if (!StringUtils.hasText(filtre)) {
            return null;
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("libelle")), motif),
                cb.like(cb.lower(root.get("description")), motif));
    }
}
