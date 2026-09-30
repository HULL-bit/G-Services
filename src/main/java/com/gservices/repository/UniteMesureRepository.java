package com.gservices.repository;

import com.gservices.entity.UniteMesure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UniteMesureRepository
        extends JpaRepository<UniteMesure, Long>, JpaSpecificationExecutor<UniteMesure> {
    boolean existsBySymboleIgnoreCase(String symbole);
    Optional<UniteMesure> findBySymboleIgnoreCase(String symbole);
    List<UniteMesure> findByEtatTrueOrderByLibelleAsc();
}
