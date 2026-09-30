package com.gservices.repository;

import com.gservices.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository
        extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Permission> findByBranchePermissionIdAndEtatTrueOrderByNiveauAsc(Long idBranche);

    List<Permission> findByEtatTrueOrderByCodeAsc();

    /** Plus grand code numérique de permission dans une branche (attribution du suivant). */
    @Query(value = "SELECT MAX(CAST(code AS integer)) FROM permission "
            + "WHERE id_branche_permission = :idBranche AND code ~ '^[0-9]+$'", nativeQuery = true)
    Integer maxCodeNumeriqueParBranche(@Param("idBranche") Long idBranche);
}
