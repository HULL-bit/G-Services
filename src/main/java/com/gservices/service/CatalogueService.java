package com.gservices.service;

import com.gservices.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Module Catalogue : hiérarchie
 * {@code CategorieService → Service (arborescent) → Catalogue → Produit → Article}
 * + référentiel {@code ProprietesArticle} / {@code UniteMesure} et la classe
 * d'association {@code VarieteArticle}.
 */
public interface CatalogueService {

    // ---------------------------------------------------- CategorieService
    Page<CategorieServiceDto> rechercherCategories(String filtre, Pageable pageable);
    List<CategorieServiceDto> categoriesActives();
    CategorieServiceDto creerCategorie(CategorieServiceDto dto);
    CategorieServiceDto modifierCategorie(Long id, CategorieServiceDto dto);
    void basculerCategorie(Long id);

    // ---------------------------------------------------- Service (arbre)
    Page<ServiceDto> rechercherServices(String filtre, Pageable pageable);
    List<ServiceDto> servicesActifs();
    /** Tous les services (actifs ou non), triés — pour l'arborescence d'administration. */
    List<ServiceDto> tousLesServices();

    /**
     * {@code true} si l'utilisateur courant est un fournisseur restreint à sa
     * catégorie de spécialité : l'IHM masque alors les onglets « transverses »
     * (Catégories, Propriétés, Unités) qui ne le concernent pas.
     */
    boolean vueRestreinteFournisseur();
    /**
     * Fournisseurs (profil {@code PRESTATAIRE}) spécialisés dans la catégorie
     * donnée : seuls candidats possibles au rôle de propriétaire d'un service de
     * cette catégorie. Liste vide si {@code idCategorie} est {@code null}.
     */
    List<com.gservices.dto.PersonneDto> fournisseursActifs(Long idCategorie);
    List<ServiceDto> servicesParCategorie(Long idCategorie);
    ServiceDto service(Long id);
    ServiceDto creerService(ServiceDto dto);
    ServiceDto modifierService(Long id, ServiceDto dto);
    void basculerService(Long id);

    // ---------------------------------------------------- Catalogue
    Page<CatalogueDto> rechercherCatalogues(String filtre, Pageable pageable);
    List<CatalogueDto> cataloguesActifs();
    List<CatalogueDto> cataloguesParService(Long idService);
    CatalogueDto creerCatalogue(CatalogueDto dto);
    CatalogueDto modifierCatalogue(Long id, CatalogueDto dto);
    void basculerCatalogue(Long id);

    // ---------------------------------------------------- Produit
    Page<ProduitDto> rechercherProduits(String filtre, Pageable pageable);
    List<ProduitDto> produitsActifs();
    ProduitDto produit(Long id);
    ProduitDto creerProduit(ProduitDto dto);
    ProduitDto modifierProduit(Long id, ProduitDto dto);
    void basculerProduit(Long id);
    /** Association « définit » : propriétés déclarées par le produit. */
    ProduitDto affecterProprietes(Long idProduit, Set<Long> proprieteIds);

    // ---------------------------------------------------- Article
    Page<ArticleDto> rechercherArticles(String filtre, Pageable pageable);
    /** Articles d'un produit (toutes déclinaisons, actives ou non) — écran Offre unifié. */
    List<ArticleDto> articlesParProduit(Long idProduit);
    ArticleDto article(Long id);
    ArticleDto creerArticle(ArticleDto dto);
    ArticleDto modifierArticle(Long id, ArticleDto dto);
    void basculerArticle(Long id);

    // ---------------------------------------------------- VarieteArticle (classe d'assoc.)
    List<VarieteArticleDto> varietesParArticle(Long idArticle);
    /** Propriétés déclarées par le produit de l'article, avec la valeur déjà saisie s'il y en a une. */
    List<VarieteArticleDto> grilleVarietes(Long idArticle);
    VarieteArticleDto enregistrerVariete(Long idArticle, Long idPropriete, String valeur);
    void basculerVariete(Long idVariete);

    // ---------------------------------------------------- Référentiel
    Page<ProprietesArticleDto> rechercherProprietes(String filtre, Pageable pageable);
    List<ProprietesArticleDto> proprietesActives();
    ProprietesArticleDto creerPropriete(ProprietesArticleDto dto);
    ProprietesArticleDto modifierPropriete(Long id, ProprietesArticleDto dto);
    void basculerPropriete(Long id);

    Page<UniteMesureDto> rechercherUnites(String filtre, Pageable pageable);
    List<UniteMesureDto> unitesActives();
    UniteMesureDto creerUnite(UniteMesureDto dto);
    UniteMesureDto modifierUnite(Long id, UniteMesureDto dto);
    void basculerUnite(Long id);
}
