package com.gservices.repository;

import com.gservices.entity.Produit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository
        extends JpaRepository<Produit, Long>, JpaSpecificationExecutor<Produit> {
    List<Produit> findByEtatTrueOrderByLibelleAsc();
    List<Produit> findByCatalogueIdAndEtatTrueOrderByLibelleAsc(Long idCatalogue);
    List<Produit> findByCatalogue_Service_CategorieService_IdAndEtatTrueOrderByLibelleAsc(Long idCategorie);
    long countByCatalogueId(Long idCatalogue);

    @EntityGraph(attributePaths = {"catalogue", "catalogue.service", "proprietes", "proprietes.uniteMesure"})
    Optional<Produit> findWithDetailsById(Long id);
}
