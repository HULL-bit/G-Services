package com.gservices.repository;

import com.gservices.entity.Continent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ContinentRepository
        extends JpaRepository<Continent, Long>, JpaSpecificationExecutor<Continent> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Continent> findByCodeIgnoreCase(String code);
    List<Continent> findByEtatTrueOrderByLibelleAsc();
}
