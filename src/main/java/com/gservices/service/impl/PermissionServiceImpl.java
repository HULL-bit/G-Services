package com.gservices.service.impl;

import com.gservices.dto.PermissionDto;
import com.gservices.entity.BranchePermission;
import com.gservices.entity.Permission;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.PermissionMapper;
import com.gservices.repository.BranchePermissionRepository;
import com.gservices.repository.PermissionRepository;
import com.gservices.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository repository;
    private final BranchePermissionRepository brancheRepository;
    private final PermissionMapper mapper;

    public PermissionServiceImpl(PermissionRepository repository,
                                 BranchePermissionRepository brancheRepository,
                                 PermissionMapper mapper) {
        this.repository = repository;
        this.brancheRepository = brancheRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERMISSION_LIRE)")
    public Page<PermissionDto> rechercher(String filtre, Pageable pageable) {
        return repository.findAll(filtreSpec(filtre), pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERMISSION_LIRE)")
    public List<PermissionDto> toutesActives() {
        return repository.findByEtatTrueOrderByCodeAsc().stream()
                .map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERMISSION_LIRE)")
    public PermissionDto parId(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERMISSION_CREER)")
    public PermissionDto creer(PermissionDto dto) {
        if (dto.getBrancheId() == null) {
            throw new BusinessException("Une permission doit être rattachée à une branche.");
        }
        BranchePermission branche = resoudreBranche(dto.getBrancheId());

        // Code auto : <code branche> + rang dans la branche (2000 -> 2001, 2002...).
        int base = entier(branche.getCode(), 0);
        Integer maxPerm = repository.maxCodeNumeriqueParBranche(branche.getId());
        int prochain = (maxPerm != null ? maxPerm : base) + 1;
        dto.setCode(String.valueOf(prochain));

        Permission p = mapper.toEntity(dto);
        p.setBranchePermission(branche);
        return mapper.toDto(repository.save(p));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERMISSION_MODIFIER)")
    public PermissionDto modifier(Long id, PermissionDto dto) {
        Permission p = charger(id);
        // Le code d'une permission est immuable (dérivé de la branche).
        dto.setCode(p.getCode());
        mapper.update(p, dto);
        // Le rattachement de branche non plus (il conditionne le code).
        return mapper.toDto(p);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERMISSION_SUPPRIMER)")
    public void basculerEtat(Long id) {
        Permission p = charger(id);
        p.setEtat(!p.isEtat());
    }

    // ------------------------------------------------------------------ util

    private Permission charger(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Permission.class, id));
    }

    private static int entier(String valeur, int defaut) {
        try {
            return Integer.parseInt(valeur.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return defaut;
        }
    }

    private BranchePermission resoudreBranche(Long idBranche) {
        if (idBranche == null) {
            return null;
        }
        return brancheRepository.findById(idBranche)
                .orElseThrow(() -> new ResourceNotFoundException(BranchePermission.class, idBranche));
    }

    private static Specification<Permission> etatActif() {
        return (root, q, cb) -> cb.isTrue(root.get("etat"));
    }

    private static Specification<Permission> filtreSpec(String filtre) {
        if (!StringUtils.hasText(filtre)) {
            return null;
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), motif),
                cb.like(cb.lower(root.get("libelle")), motif),
                cb.like(cb.lower(root.get("actionAutorisee")), motif));
    }
}
