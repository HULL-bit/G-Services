package com.gservices.repository;

import com.gservices.entity.BranchePermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BranchePermissionRepository
        extends JpaRepository<BranchePermission, Long>, JpaSpecificationExecutor<BranchePermission> {

    Optional<BranchePermission> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    /** Branches actives + leurs permissions, pour la construction du menu. */
    @EntityGraph(attributePaths = {"permissions"})
    List<BranchePermission> findByEtatTrueOrderByNiveauAsc();

    /** Plus grand code numérique de branche (pour attribuer le suivant : +1000). */
    @Query(value = "SELECT MAX(CAST(code AS integer)) FROM branche_permission WHERE code ~ '^[0-9]+$'",
            nativeQuery = true)
    Integer maxCodeNumerique();
}
