package com.gservices.repository;

import com.gservices.entity.Article;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository
        extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    boolean existsByReferenceIgnoreCase(String reference);
    Optional<Article> findByReferenceIgnoreCase(String reference);
    List<Article> findByProduitIdAndEtatTrueOrderByReferenceAsc(Long idProduit);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"produit", "varietes"})
    List<Article> findByProduitIdOrderByReferenceAsc(Long idProduit);
    long countByProduitId(Long idProduit);
    java.util.List<Article> findByStockIsNullAndEtatTrueOrderByReferenceAsc();

    @EntityGraph(attributePaths = {"produit", "varietes", "varietes.proprietesArticle",
            "varietes.proprietesArticle.uniteMesure"})
    Optional<Article> findWithVarietesById(Long id);

    /** Articles actifs et disponibles d'un service — tunnel de commande à distance. */
    @org.springframework.data.jpa.repository.Query(
        "select a from Article a "
      + "where a.etat = true and a.disponibilite = true "
      + "and a.produit.catalogue.service.id = :idService "
      + "order by a.produit.libelle, a.reference")
    List<Article> findCommandablesParService(Long idService);

    /** Id du service auquel l'article est rattaché (Article → Produit → Catalogue → Service). */
    @org.springframework.data.jpa.repository.Query(
        "select a.produit.catalogue.service.id from Article a where a.id = :idArticle")
    Optional<Long> serviceIdDe(Long idArticle);
}
