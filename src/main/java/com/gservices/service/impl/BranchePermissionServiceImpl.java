package com.gservices.service.impl;

import com.gservices.dto.BranchePermissionDto;
import com.gservices.entity.BranchePermission;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.BranchePermissionMapper;
import com.gservices.repository.BranchePermissionRepository;
import com.gservices.service.BranchePermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class BranchePermissionServiceImpl implements BranchePermissionService {

    private final BranchePermissionRepository repository;
    private final BranchePermissionMapper mapper;

    public BranchePermissionServiceImpl(BranchePermissionRepository repository,
                                        BranchePermissionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).BRANCHE_LIRE)")
    public Page<BranchePermissionDto> rechercher(String filtre, Pageable pageable) {
        return repository.findAll(filtreSpec(filtre), pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).BRANCHE_LIRE)")
    public List<BranchePermissionDto> toutesActives() {
        return repository.findAll(Specification.where(actif()), Sort.by("niveau")).stream()
                .map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).BRANCHE_LIRE)")
    public BranchePermissionDto parId(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).BRANCHE_CREER)")
    public BranchePermissionDto creer(BranchePermissionDto dto) {
        // Code auto : prochain multiple de 1000 (1000, 2000, 3000...).
        Integer max = repository.maxCodeNumerique();
        int prochain = (max == null ? 0 : max / 1000 * 1000) + 1000;
        dto.setCode(String.valueOf(prochain));

        BranchePermission b = mapper.toEntity(dto);
        return mapper.toDto(repository.save(b));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).BRANCHE_MODIFIER)")
    public BranchePermissionDto modifier(Long id, BranchePermissionDto dto) {
        BranchePermission b = charger(id);
        // Le code d'une branche est immuable (auto-généré à la création).
        dto.setCode(b.getCode());
        mapper.update(b, dto);
        return mapper.toDto(b);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).BRANCHE_SUPPRIMER)")
    public void basculerEtat(Long id) {
        BranchePermission b = charger(id);
        b.setEtat(!b.isEtat());
    }

    private BranchePermission charger(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BranchePermission.class, id));
    }

    private static Specification<BranchePermission> actif() {
        return (root, q, cb) -> cb.isTrue(root.get("etat"));
    }

    private static Specification<BranchePermission> filtreSpec(String filtre) {
        if (!StringUtils.hasText(filtre)) {
            return null;
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), motif),
                cb.like(cb.lower(root.get("libelle")), motif));
    }
}
