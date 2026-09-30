package com.gservices.repository;

import com.gservices.entity.CategorieService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CategorieServiceRepository
        extends JpaRepository<CategorieService, Long>, JpaSpecificationExecutor<CategorieService> {
    boolean existsByLibelleIgnoreCase(String libelle);
    List<CategorieService> findByEtatTrueOrderByOrdreAffichageAscLibelleAsc();
}
