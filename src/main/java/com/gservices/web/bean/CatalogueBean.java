package com.gservices.web.bean;

import com.gservices.dto.*;
import com.gservices.service.CatalogueService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.DefaultTreeNode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Écran d'administration unique du module Catalogue. Onglets : Catégories,
 * Arborescence des services (profondeur illimitée), Catalogues, Offres
 * (Produit + ses Articles dans le même écran), Propriétés, Unités.
 */
@Component("catalogueBean")
@Scope("view")
public class CatalogueBean extends AbstractAdminBean {

    private final transient CatalogueService service;

    // Filtres
    private String filtreCategorie;
    private String filtreCatalogue;
    private String filtreProduit;
    private String filtrePropriete;
    private String filtreUnite;

    // Modèles paginés
    private LazyDataModel<CategorieServiceDto> categories;
    private LazyDataModel<CatalogueDto> catalogues;
    private LazyDataModel<ProduitDto> produits;
    private LazyDataModel<ProprietesArticleDto> proprietes;
    private LazyDataModel<UniteMesureDto> unites;

    // Sélections
    private CategorieServiceDto categorie = new CategorieServiceDto();
    private ServiceDto unService = new ServiceDto();
    private CatalogueDto catalogue = new CatalogueDto();
    private ProduitDto produit = new ProduitDto();
    private ArticleDto article = new ArticleDto();
    private ProprietesArticleDto propriete = new ProprietesArticleDto();
    private UniteMesureDto unite = new UniteMesureDto();

    // Référentiels plats
    private List<CategorieServiceDto> refCategories = new ArrayList<>();
    private List<ServiceDto> refServices = new ArrayList<>();
    private List<com.gservices.dto.PersonneDto> refFournisseurs = new ArrayList<>();
    private List<CatalogueDto> refCatalogues = new ArrayList<>();
    private List<ProduitDto> refProduits = new ArrayList<>();
    private List<UniteMesureDto> refUnites = new ArrayList<>();
    private List<ProprietesArticleDto> refProprietes = new ArrayList<>();

    // « Définir les propriétés » d'un produit
    private ProduitDto produitProprietes;
    private Set<Long> proprietesCochees = new LinkedHashSet<>();

    // Grille de variétés d'un article (classe d'association VarieteArticle)
    private ArticleDto articleVarietes;
    private List<VarieteArticleDto> grilleVarietes = new ArrayList<>();

    // Arborescence des services (Catégorie › Service › Sous-service › …)
    private TreeNode<ServiceDto> arbreServices;

    // Écran Offre unifié : les articles du produit en cours d'édition
    private List<ArticleDto> articlesDuProduit = new ArrayList<>();

    public CatalogueBean(CatalogueService service) {
        this.service = service;
    }

    /** Vue « fournisseur » : masque les onglets transverses (Catégories, Propriétés, Unités). */
    private boolean vueRestreinte;

    public boolean isVueRestreinte() {
        return vueRestreinte;
    }

    @PostConstruct
    void init() {
        categories = new LazyModelService<>(service::rechercherCategories, CategorieServiceDto::getId, () -> filtreCategorie);
        catalogues = new LazyModelService<>(service::rechercherCatalogues, CatalogueDto::getId, () -> filtreCatalogue);
        produits = new LazyModelService<>(service::rechercherProduits, ProduitDto::getId, () -> filtreProduit);
        proprietes = new LazyModelService<>(service::rechercherProprietes, ProprietesArticleDto::getId, () -> filtrePropriete);
        unites = new LazyModelService<>(service::rechercherUnites, UniteMesureDto::getId, () -> filtreUnite);
        try {
            vueRestreinte = service.vueRestreinteFournisseur();
        } catch (RuntimeException e) {
            vueRestreinte = false;
        }
        rechargerReferentiels();
        rebatirArbre();
    }

    private void rechargerReferentiels() {
        refCategories = safe(service::categoriesActives);
        refServices = safe(service::servicesActifs);
        refCatalogues = safe(service::cataloguesActifs);
        refProduits = safe(service::produitsActifs);
        refUnites = safe(service::unitesActives);
        refProprietes = safe(service::proprietesActives);
    }

    /** Recharge la liste des propriétaires possibles = fournisseurs spécialisés dans la catégorie du service édité. */
    private void rechargerFournisseurs() {
        Long idCat = unService == null ? null : unService.getCategorieServiceId();
        refFournisseurs = idCat == null ? new ArrayList<>()
                : safe(() -> service.fournisseursActifs(idCat));
        if (unService != null && unService.getProprietaireId() != null
                && refFournisseurs.stream().noneMatch(f -> f.getId().equals(unService.getProprietaireId()))) {
            unService.setProprietaireId(null);   // l'ancien propriétaire n'est plus valide pour cette catégorie
        }
    }

    /** Écouteur AJAX : au changement de catégorie dans le dialogue Service, recharge les propriétaires possibles. */
    public void onCategorieServiceChoisie() {
        rechargerFournisseurs();
    }

    /** Construit l'arbre Catégorie › Service › Sous-service › … (profondeur illimitée). */
    private void rebatirArbre() {
        TreeNode<ServiceDto> racine = new DefaultTreeNode<>(new ServiceDto(), null);
        racine.setExpanded(true);
        List<ServiceDto> tous = safe(service::tousLesServices);

        // 1er niveau : les catégories
        Map<Long, TreeNode<ServiceDto>> noeudsCategorie = new LinkedHashMap<>();
        for (CategorieServiceDto c : safe(service::categoriesActives)) {
            ServiceDto entete = new ServiceDto();
            entete.setId(-c.getId());               // id négatif = nœud « catégorie »
            entete.setLibelle(c.getLibelle());
            entete.setCategorieServiceId(c.getId());
            entete.setCategorieServiceLibelle(c.getLibelle());
            TreeNode<ServiceDto> n = new DefaultTreeNode<>("categorie", entete, racine);
            n.setExpanded(true);
            noeudsCategorie.put(c.getId(), n);
        }

        // services rangés par parent
        Map<Long, List<ServiceDto>> parParent = new LinkedHashMap<>();
        List<ServiceDto> racines = new ArrayList<>();
        for (ServiceDto s : tous) {
            if (s.getParentId() == null) {
                racines.add(s);
            } else {
                parParent.computeIfAbsent(s.getParentId(), k -> new ArrayList<>()).add(s);
            }
        }
        for (ServiceDto s : racines) {
            TreeNode<ServiceDto> parent = noeudsCategorie.get(s.getCategorieServiceId());
            attacher(s, parent != null ? parent : racine, parParent);
        }
        this.arbreServices = racine;
    }

    private void attacher(ServiceDto s, TreeNode<ServiceDto> parent, Map<Long, List<ServiceDto>> parParent) {
        TreeNode<ServiceDto> n = new DefaultTreeNode<>("service", s, parent);
        n.setExpanded(true);
        for (ServiceDto enfant : parParent.getOrDefault(s.getId(), List.of())) {
            attacher(enfant, n, parParent);
        }
    }

    // ============================================================ Catégorie ===

    public void nouvelleCategorie() { categorie = new CategorieServiceDto(); }
    public void editerCategorie(CategorieServiceDto d) { categorie = d; }

    public void enregistrerCategorie() {
        boolean ok = executer(() -> {
            if (categorie.getId() == null) {
                service.creerCategorie(categorie);
            } else {
                service.modifierCategorie(categorie.getId(), categorie);
            }
            rechargerReferentiels();
        });
        termine(ok, "cat.categorie.enregistre");
    }

    public void basculerCategorie(CategorieServiceDto d) {
        if (executer(() -> service.basculerCategorie(d.getId()))) {
            rechargerReferentiels();
        }
    }

    // ============================================================ Service =====

    public void nouveauService() {
        unService = new ServiceDto();
        rechargerFournisseurs();
    }

    /** Crée un service à la racine d'une catégorie. */
    public void nouveauServiceRacine(Long idCategorie) {
        unService = new ServiceDto();
        unService.setCategorieServiceId(idCategorie);
        rechargerFournisseurs();
    }

    /** Crée un sous-service (sous-catégorie) sous le service donné. */
    public void nouveauSousService(ServiceDto parent) {
        unService = new ServiceDto();
        unService.setParentId(parent.getId());
        unService.setCategorieServiceId(parent.getCategorieServiceId());
        rechargerFournisseurs();
    }

    public void editerService(ServiceDto d) {
        unService = service.service(d.getId());
        rechargerFournisseurs();
    }

    public void enregistrerService() {
        boolean ok = executer(() -> {
            if (unService.getId() == null) {
                service.creerService(unService);
            } else {
                service.modifierService(unService.getId(), unService);
            }
            rechargerReferentiels();
            rebatirArbre();
        });
        termine(ok, "cat.service.enregistre");
    }

    public void basculerService(ServiceDto d) {
        if (executer(() -> service.basculerService(d.getId()))) {
            rechargerReferentiels();
            rebatirArbre();
        }
    }

    // ============================================================ Catalogue ===

    public void nouveauCatalogue() { catalogue = new CatalogueDto(); }
    public void editerCatalogue(CatalogueDto d) { catalogue = d; }

    public void enregistrerCatalogue() {
        boolean ok = executer(() -> {
            if (catalogue.getId() == null) {
                service.creerCatalogue(catalogue);
            } else {
                service.modifierCatalogue(catalogue.getId(), catalogue);
            }
            rechargerReferentiels();
        });
        termine(ok, "cat.catalogue.enregistre");
    }

    public void basculerCatalogue(CatalogueDto d) {
        if (executer(() -> service.basculerCatalogue(d.getId()))) {
            rechargerReferentiels();
        }
    }

    // ============================================================ Produit =====

    public void nouveauProduit() {
        produit = new ProduitDto();
        articlesDuProduit = new ArrayList<>();
    }

    public void editerProduit(ProduitDto d) {
        produit = service.produit(d.getId());
        articlesDuProduit = safe(() -> service.articlesParProduit(d.getId()));
    }

    public void enregistrerProduit() {
        boolean ok = executer(() -> {
            if (produit.getId() == null) {
                ProduitDto cree = service.creerProduit(produit);
                produit.setId(cree.getId());
            } else {
                service.modifierProduit(produit.getId(), produit);
            }
            rechargerReferentiels();
            articlesDuProduit = safe(() -> service.articlesParProduit(produit.getId()));
        });
        // on garde le dialogue ouvert pour permettre l'ajout d'articles
        if (ok) {
            info(msg("cat.produit.enregistre"));
            callback("produitOk", true);
        }
    }

    public void basculerProduit(ProduitDto d) {
        if (executer(() -> service.basculerProduit(d.getId()))) {
            rechargerReferentiels();
        }
    }

    public void gererProprietesProduit(ProduitDto d) {
        boolean ok = executer(() -> {
            produitProprietes = service.produit(d.getId());
            proprietesCochees = new LinkedHashSet<>(produitProprietes.getProprieteIds());
        });
        if (ok) {
            callback("props", true);
        }
    }

    public void enregistrerProprietesProduit() {
        boolean ok = executer(() -> {
            service.affecterProprietes(produitProprietes.getId(), proprietesCochees);
            rechargerReferentiels();
        });
        if (ok) {
            info(msg("cat.produit.proprietesOk"));
            callback("ok", true);
        }
    }

    // ================= Articles (déclinaisons du produit en cours) ============

    /** Prépare un nouvel article rattaché au produit ouvert dans le dialogue Offre. */
    public void nouvelArticle() {
        article = new ArticleDto();
        article.setDevise("XOF");
        article.setDisponibilite(true);
        article.setProduitId(produit.getId());
    }

    public void editerArticle(ArticleDto d) {
        article = service.article(d.getId());
    }

    public void enregistrerArticle() {
        boolean ok = executer(() -> {
            article.setProduitId(produit.getId());
            if (article.getId() == null) {
                service.creerArticle(article);
            } else {
                service.modifierArticle(article.getId(), article);
            }
            articlesDuProduit = safe(() -> service.articlesParProduit(produit.getId()));
            rechargerReferentiels();
        });
        if (ok) {
            info(msg("cat.article.enregistre"));
            callback("articleOk", true);
        }
    }

    public void basculerArticle(ArticleDto d) {
        if (executer(() -> service.basculerArticle(d.getId()))) {
            articlesDuProduit = safe(() -> service.articlesParProduit(produit.getId()));
            callback("articleOk", true);
        }
    }

    // ---- Variétés (classe d'association) ----

    public void gererVarietes(ArticleDto d) {
        boolean ok = executer(() -> {
            articleVarietes = service.article(d.getId());
            grilleVarietes = service.grilleVarietes(d.getId());
        });
        if (ok) {
            callback("var", true);
        }
    }

    public void enregistrerVariete(VarieteArticleDto ligne) {
        boolean ok = executer(() -> {
            service.enregistrerVariete(articleVarietes.getId(), ligne.getProprietesArticleId(), ligne.getValeur());
            grilleVarietes = service.grilleVarietes(articleVarietes.getId());
        });
        if (ok) {
            info(msg("cat.variete.enregistre"));
            callback("okvar", true);
        }
    }

    public void basculerVariete(VarieteArticleDto ligne) {
        if (ligne.getId() == null) {
            return;
        }
        if (executer(() -> service.basculerVariete(ligne.getId()))) {
            grilleVarietes = service.grilleVarietes(articleVarietes.getId());
            callback("okvar", true);
        }
    }

    // ============================================================ Propriété ===

    public void nouvellePropriete() { propriete = new ProprietesArticleDto(); }
    public void editerPropriete(ProprietesArticleDto d) { propriete = d; }

    public void enregistrerPropriete() {
        boolean ok = executer(() -> {
            if (propriete.getId() == null) {
                service.creerPropriete(propriete);
            } else {
                service.modifierPropriete(propriete.getId(), propriete);
            }
            rechargerReferentiels();
        });
        termine(ok, "cat.propriete.enregistre");
    }

    public void basculerPropriete(ProprietesArticleDto d) {
        if (executer(() -> service.basculerPropriete(d.getId()))) {
            rechargerReferentiels();
        }
    }

    // ============================================================ Unité ======

    public void nouvelleUnite() { unite = new UniteMesureDto(); }
    public void editerUnite(UniteMesureDto d) { unite = d; }

    public void enregistrerUnite() {
        boolean ok = executer(() -> {
            if (unite.getId() == null) {
                service.creerUnite(unite);
            } else {
                service.modifierUnite(unite.getId(), unite);
            }
            rechargerReferentiels();
        });
        termine(ok, "cat.unite.enregistre");
    }

    public void basculerUnite(UniteMesureDto d) {
        if (executer(() -> service.basculerUnite(d.getId()))) {
            rechargerReferentiels();
        }
    }

    // ============================================================ util =======

    public String[] getTypesSaisie() {
        return new String[] {"TEXTE", "NOMBRE", "LISTE", "BOOLEEN", "DATE"};
    }

    public String[] getModesVente() {
        return new String[] {"UNITE", "LOT", "POIDS", "VOLUME", "HEURE", "NUIT", "FORFAIT", "ABONNEMENT"};
    }

    private void termine(boolean ok, String cle) {
        if (ok) {
            info(msg(cle));
            callback("ok", true);
        }
    }

    private static <T> List<T> safe(Supplier<List<T>> s) {
        try {
            return s.get();
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    // ---------------------------------------------------------- accessors ----

    public String getFiltreCategorie() { return filtreCategorie; }
    public void setFiltreCategorie(String v) { this.filtreCategorie = v; }
    public String getFiltreCatalogue() { return filtreCatalogue; }
    public void setFiltreCatalogue(String v) { this.filtreCatalogue = v; }
    public String getFiltreProduit() { return filtreProduit; }
    public void setFiltreProduit(String v) { this.filtreProduit = v; }
    public String getFiltrePropriete() { return filtrePropriete; }
    public void setFiltrePropriete(String v) { this.filtrePropriete = v; }
    public String getFiltreUnite() { return filtreUnite; }
    public void setFiltreUnite(String v) { this.filtreUnite = v; }

    public LazyDataModel<CategorieServiceDto> getCategories() { return categories; }
    public LazyDataModel<CatalogueDto> getCatalogues() { return catalogues; }
    public LazyDataModel<ProduitDto> getProduits() { return produits; }
    public LazyDataModel<ProprietesArticleDto> getProprietes() { return proprietes; }
    public LazyDataModel<UniteMesureDto> getUnites() { return unites; }

    public TreeNode<ServiceDto> getArbreServices() { return arbreServices; }
    public List<ArticleDto> getArticlesDuProduit() { return articlesDuProduit; }

    public CategorieServiceDto getCategorie() { return categorie; }
    public void setCategorie(CategorieServiceDto v) { this.categorie = v; }
    public ServiceDto getUnService() { return unService; }
    public void setUnService(ServiceDto v) { this.unService = v; }
    public CatalogueDto getCatalogue() { return catalogue; }
    public void setCatalogue(CatalogueDto v) { this.catalogue = v; }
    public ProduitDto getProduit() { return produit; }
    public void setProduit(ProduitDto v) { this.produit = v; }
    public ArticleDto getArticle() { return article; }
    public void setArticle(ArticleDto v) { this.article = v; }
    public ProprietesArticleDto getPropriete() { return propriete; }
    public void setPropriete(ProprietesArticleDto v) { this.propriete = v; }
    public UniteMesureDto getUnite() { return unite; }
    public void setUnite(UniteMesureDto v) { this.unite = v; }

    public List<CategorieServiceDto> getRefCategories() { return refCategories; }
    public List<ServiceDto> getRefServices() { return refServices; }
    public List<com.gservices.dto.PersonneDto> getRefFournisseurs() { return refFournisseurs; }
    public List<CatalogueDto> getRefCatalogues() { return refCatalogues; }
    public List<ProduitDto> getRefProduits() { return refProduits; }
    public List<UniteMesureDto> getRefUnites() { return refUnites; }
    public List<ProprietesArticleDto> getRefProprietes() { return refProprietes; }

    public ProduitDto getProduitProprietes() { return produitProprietes; }
    public Set<Long> getProprietesCochees() { return proprietesCochees; }
    public void setProprietesCochees(Set<Long> v) { this.proprietesCochees = v; }

    public ArticleDto getArticleVarietes() { return articleVarietes; }
    public List<VarieteArticleDto> getGrilleVarietes() { return grilleVarietes; }
}
