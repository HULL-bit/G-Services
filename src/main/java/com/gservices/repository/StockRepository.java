package com.gservices.repository;

import com.gservices.entity.Stock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StockRepository
        extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {

    boolean existsByArticleId(Long idArticle);
    Optional<Stock> findByArticleId(Long idArticle);

    @EntityGraph(attributePaths = {"article", "article.produit", "lots", "lots.mois", "lots.mois.annee"})
    Optional<Stock> findWithLotsById(Long id);

    @EntityGraph(attributePaths = {"article", "lots"})
    java.util.List<Stock> findByEtatTrue();
}
