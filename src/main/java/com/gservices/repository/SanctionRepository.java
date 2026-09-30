package com.gservices.repository;

import com.gservices.entity.Sanction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SanctionRepository
        extends JpaRepository<Sanction, Long>, JpaSpecificationExecutor<Sanction> {

    @EntityGraph(attributePaths = {"service", "service.categorieService", "admin"})
    Optional<Sanction> findDetailById(Long id);

    Optional<Sanction> findFirstByServiceIdAndDateLeveeIsNullOrderByDateSanctionDesc(Long idService);
}
