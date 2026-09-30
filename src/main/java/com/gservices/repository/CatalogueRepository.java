package com.gservices.repository;

import com.gservices.entity.Catalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CatalogueRepository
        extends JpaRepository<Catalogue, Long>, JpaSpecificationExecutor<Catalogue> {
    List<Catalogue> findByEtatTrueOrderByLibelleAsc();
    List<Catalogue> findByServiceIdAndEtatTrueOrderByLibelleAsc(Long idService);
    List<Catalogue> findByService_CategorieService_IdAndEtatTrueOrderByLibelleAsc(Long idCategorie);
    long countByServiceId(Long idService);
}
