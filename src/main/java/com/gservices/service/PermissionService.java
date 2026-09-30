package com.gservices.service;

import com.gservices.dto.PermissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PermissionService {

    Page<PermissionDto> rechercher(String filtre, Pageable pageable);

    List<PermissionDto> toutesActives();

    PermissionDto parId(Long id);

    PermissionDto creer(PermissionDto dto);

    PermissionDto modifier(Long id, PermissionDto dto);

    /** Active ↔ désactive (soft-delete). */
    void basculerEtat(Long id);
}
