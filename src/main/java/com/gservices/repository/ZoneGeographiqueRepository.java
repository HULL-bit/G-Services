package com.gservices.repository;

import com.gservices.entity.ZoneGeographique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ZoneGeographiqueRepository
        extends JpaRepository<ZoneGeographique, Long>, JpaSpecificationExecutor<ZoneGeographique> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<ZoneGeographique> findByCodeIgnoreCase(String code);
    List<ZoneGeographique> findByContinentIdAndEtatTrueOrderByLibelleAsc(Long idContinent);
    List<ZoneGeographique> findByEtatTrueOrderByLibelleAsc();
    long countByContinentId(Long idContinent);
}
