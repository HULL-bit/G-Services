package com.gservices.repository;

import com.gservices.entity.Signalement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SignalementRepository
        extends JpaRepository<Signalement, Long>, JpaSpecificationExecutor<Signalement> {

    @EntityGraph(attributePaths = {"personne", "service", "service.categorieService"})
    Optional<Signalement> findDetailById(Long id);

    long countByStatutAndEtatTrue(com.gservices.entity.StatutSignalement statut);
}
