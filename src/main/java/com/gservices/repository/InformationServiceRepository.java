package com.gservices.repository;

import com.gservices.entity.InformationService;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InformationServiceRepository
        extends JpaRepository<InformationService, Long>, JpaSpecificationExecutor<InformationService> {

    boolean existsByServiceId(Long idService);

    @EntityGraph(attributePaths = {"service", "position", "position.ville",
            "horaires", "horaires.jour"})
    Optional<InformationService> findWithDetailsById(Long id);

    long countByPositionId(Long idPosition);
}
