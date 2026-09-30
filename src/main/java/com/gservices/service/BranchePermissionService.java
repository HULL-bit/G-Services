package com.gservices.service;

import com.gservices.dto.BranchePermissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BranchePermissionService {

    Page<BranchePermissionDto> rechercher(String filtre, Pageable pageable);

    List<BranchePermissionDto> toutesActives();

    BranchePermissionDto parId(Long id);

    BranchePermissionDto creer(BranchePermissionDto dto);

    BranchePermissionDto modifier(Long id, BranchePermissionDto dto);

    void basculerEtat(Long id);
}
