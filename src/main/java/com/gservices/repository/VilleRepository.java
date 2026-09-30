package com.gservices.repository;

import com.gservices.entity.Ville;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VilleRepository
        extends JpaRepository<Ville, Long>, JpaSpecificationExecutor<Ville> {
    List<Ville> findByRegionIdAndEtatTrueOrderByLibelleAsc(Long idRegion);
    long countByRegionId(Long idRegion);

    Optional<Ville> findFirstByLibelleIgnoreCase(String libelle);

    @EntityGraph(attributePaths = {
            "positions", "region", "region.pays",
            "region.pays.zoneGeographique", "region.pays.zoneGeographique.continent"
    })
    Optional<Ville> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"positions"})
    List<Ville> findByEtatTrueAndPositionsIsNotEmpty();
}
