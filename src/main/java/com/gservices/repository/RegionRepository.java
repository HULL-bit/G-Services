package com.gservices.repository;

import com.gservices.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RegionRepository
        extends JpaRepository<Region, Long>, JpaSpecificationExecutor<Region> {
    List<Region> findByPaysIdAndEtatTrueOrderByLibelleAsc(Long idPays);
    List<Region> findByEtatTrueOrderByLibelleAsc();
    long countByPaysId(Long idPays);
}
