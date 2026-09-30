package com.gservices.repository;

import com.gservices.entity.RoleProfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RoleProfilRepository
        extends JpaRepository<RoleProfil, Long>, JpaSpecificationExecutor<RoleProfil> {

    List<RoleProfil> findByProfilId(Long idProfil);

    List<RoleProfil> findByPermissionId(Long idPermission);

    Optional<RoleProfil> findByProfilIdAndPermissionId(Long idProfil, Long idPermission);

    boolean existsByProfilIdAndPermissionId(Long idProfil, Long idPermission);

    void deleteByProfilIdAndPermissionId(Long idProfil, Long idPermission);
}
