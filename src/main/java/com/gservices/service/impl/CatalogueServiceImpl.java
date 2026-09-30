package com.gservices.service.impl;

import com.gservices.dto.*;
import com.gservices.entity.*;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.*;
import com.gservices.repository.*;
import com.gservices.service.CatalogueService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implémentation du module Catalogue. Un seul service cohérent (consommé par
 * l'écran unique « Catalogue »), gardé par les permissions {@code CATALOGUE_*}.
 * La désactivation est un <em>soft-delete</em> refusé tant qu'un niveau inférieur
 * actif subsiste.
 */
@org.springframework.stereotype.Service
@Transactional
public class CatalogueServiceImpl implements CatalogueService {

    private final CategorieServiceRepository categorieRepo;
    private final ServiceRepository serviceRepo;
    private final CatalogueRepository catalogueRepo;
    private final ProduitRepository produitRepo;
    private final ArticleRepository articleRepo;
    private final ProprietesArticleRepository proprieteRepo;
    private final UniteMesureRepository uniteRepo;
    private final VarieteArticleRepository varieteRepo;
    private final com.gservices.repository.PersonneRepository personneRepo;
    private final com.gservices.security.PerimetreFournisseur perimetre;

    private final CategorieServiceMapper categorieMapper;
    private final ServiceMapper serviceMapper;
    private final CatalogueMapper catalogueMapper;
    private final ProduitMapper produitMapper;
    private final ArticleMapper articleMapper;
    private final ProprietesArticleMapper proprieteMapper;
    private final UniteMesureMapper uniteMapper;
    private final VarieteArticleMapper varieteMapper;

    public CatalogueServiceImpl(CategorieServiceRepository categorieRepo, ServiceRepository serviceRepo,
                                CatalogueRepository catalogueRepo, ProduitRepository produitRepo,
                                ArticleRepository articleRepo, ProprietesArticleRepository proprieteRepo,
                                UniteMesureRepository uniteRepo, VarieteArticleRepository varieteRepo,
                                com.gservices.repository.PersonneRepository personneRepo,
                                com.gservices.security.PerimetreFournisseur perimetre,
                                CategorieServiceMapper categorieMapper, ServiceMapper serviceMapper,
                                CatalogueMapper catalogueMapper, ProduitMapper produitMapper,
                                ArticleMapper articleMapper, ProprietesArticleMapper proprieteMapper,
                                UniteMesureMapper uniteMapper, VarieteArticleMapper varieteMapper) {
        this.categorieRepo = categorieRepo;
        this.serviceRepo = serviceRepo;
        this.catalogueRepo = catalogueRepo;
        this.produitRepo = produitRepo;
        this.articleRepo = articleRepo;
        this.proprieteRepo = proprieteRepo;
        this.uniteRepo = uniteRepo;
        this.varieteRepo = varieteRepo;
        this.personneRepo = personneRepo;
        this.perimetre = perimetre;
        this.categorieMapper = categorieMapper;
        this.serviceMapper = serviceMapper;
        this.catalogueMapper = catalogueMapper;
        this.produitMapper = produitMapper;
        this.articleMapper = articleMapper;
        this.proprieteMapper = proprieteMapper;
        this.uniteMapper = uniteMapper;
        this.varieteMapper = varieteMapper;
    }

    // ==================================================== CategorieService ====

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public Page<CategorieServiceDto> rechercherCategories(String filtre, Pageable pageable) {
        return categorieRepo.findAll(etendre(texteSpec(filtre, "libelle", "description"), perimetreCategorieSpec()), pageable)
                .map(categorieMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<CategorieServiceDto> categoriesActives() {
        Long perim = perimetre.idCategorie();
        return categorieRepo.findByEtatTrueOrderByOrdreAffichageAscLibelleAsc().stream()
                .filter(c -> perim == null || perim.equals(c.getId()))
                .map(categorieMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_CREER)")
    public CategorieServiceDto creerCategorie(CategorieServiceDto dto) {
        if (categorieRepo.existsByLibelleIgnoreCase(dto.getLibelle())) {
            throw new BusinessException("La catégorie « " + dto.getLibelle() + " » existe déjà.");
        }
        return categorieMapper.toDto(categorieRepo.save(categorieMapper.toEntity(dto)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public CategorieServiceDto modifierCategorie(Long id, CategorieServiceDto dto) {
        CategorieService c = categorieRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CategorieService.class, id));
        categorieMapper.update(c, dto);
        return categorieMapper.toDto(c);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_SUPPRIMER)")
    public void basculerCategorie(Long id) {
        CategorieService c = categorieRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CategorieService.class, id));
        if (c.isEtat() && serviceRepo.countByCategorieServiceId(id) > 0) {
            throw new BusinessException("Cette catégorie contient des services : désactivez-les d'abord.");
        }
        c.setEtat(!c.isEtat());
    }

    // ==================================================== Service =============

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public Page<ServiceDto> rechercherServices(String filtre, Pageable pageable) {
        return serviceRepo.findAll(etendre(texteSpec(filtre, "libelle", "description"), perimetreServiceSpec()), pageable)
                .map(serviceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<ServiceDto> servicesActifs() {
        Set<Long> autorises = perimetre.servicesAutorises();
        return serviceRepo.findByEtatTrueOrderByLibelleAsc().stream()
                .filter(s -> autorises == null || autorises.contains(s.getId()))
                .map(serviceMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public boolean vueRestreinteFournisseur() {
        return perimetre.estRestreint();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<ServiceDto> tousLesServices() {
        Set<Long> autorises = perimetre.servicesAutorises();
        return serviceRepo.findAllByOrderByCategorieServiceLibelleAscLibelleAsc().stream()
                .filter(s -> autorises == null || autorises.contains(s.getId()))
                .map(serviceMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<ServiceDto> servicesParCategorie(Long idCategorie) {
        if (idCategorie == null || !perimetre.couvreCategorie(idCategorie)) {
            return List.of();
        }
        Set<Long> autorises = perimetre.servicesAutorises();
        return serviceRepo.findByCategorieServiceIdAndEtatTrueOrderByLibelleAsc(idCategorie).stream()
                .filter(s -> autorises == null || autorises.contains(s.getId()))
                .map(serviceMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public ServiceDto service(Long id) {
        return serviceMapper.toDto(serviceRepo.findWithParentsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Service.class, id)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_CREER)")
    public ServiceDto creerService(ServiceDto dto) {
        Long perim = perimetre.idCategorie();
        if (perim != null) {
            // un fournisseur ne crée pas de nouveau service racine : uniquement
            // un sous-service SOUS un service qu'il possède déjà.
            if (dto.getParentId() == null || horsPerimetreService(dto.getParentId())) {
                throw new BusinessException("Vous ne pouvez créer qu'un sous-service de votre propre service.");
            }
            dto.setCategorieServiceId(perim);
        }
        perimetre.exigerCategorie(dto.getCategorieServiceId());
        Service s = serviceMapper.toEntity(dto);
        s.setCategorieService(chargerCategorie(dto.getCategorieServiceId()));
        s.setParent(resoudreParent(dto.getParentId(), null));
        s.setProprietaire(resoudreProprietaire(dto.getProprietaireId(), dto.getCategorieServiceId(), null));
        // créé par un fournisseur → en attente de validation ; par un admin → validé
        s.setValide(!perimetre.estRestreint());
        return serviceMapper.toDto(serviceRepo.save(s));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public ServiceDto modifierService(Long id, ServiceDto dto) {
        Service s = serviceRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Service.class, id));
        exigerPerimetreService(s);
        Long perim = perimetre.idCategorie();
        if (perim != null) {
            dto.setCategorieServiceId(perim);
        }
        perimetre.exigerCategorie(dto.getCategorieServiceId());
        serviceMapper.update(s, dto);
        s.setCategorieService(chargerCategorie(dto.getCategorieServiceId()));
        s.setParent(resoudreParent(dto.getParentId(), id));
        s.setProprietaire(resoudreProprietaire(dto.getProprietaireId(), dto.getCategorieServiceId(), id));
        return serviceMapper.toDto(s);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<com.gservices.dto.PersonneDto> fournisseursActifs(Long idCategorie) {
        if (idCategorie == null) {
            return List.of();
        }
        return personneRepo.findFournisseursParSpecialite(idCategorie).stream().map(p -> {
            com.gservices.dto.PersonneDto d = new com.gservices.dto.PersonneDto();
            d.setId(p.getId());
            d.setNom(p.getNom());
            d.setPrenom(p.getPrenom());
            d.setLogin(p.getLogin());
            d.setCategorieSpecialiteId(idCategorie);
            d.setDisponible(serviceRepo.findByProprietaireId(p.getId()).isEmpty());
            return d;
        }).toList();
    }

    /**
     * Résout le propriétaire d'un service. Contrôles :
     * <ul>
     *   <li>c'est un fournisseur spécialisé dans la catégorie du service ;</li>
     *   <li>il ne gère pas déjà un <strong>autre</strong> service racine
     *       (« 1 fournisseur = 1 service »).</li>
     * </ul>
     */
    private Personne resoudreProprietaire(Long idPersonne, Long idCategorie, Long idServiceCourant) {
        if (idPersonne == null) {
            return null;
        }
        Personne p = personneRepo.findWithSpecialiteById(idPersonne)
                .orElseThrow(() -> new ResourceNotFoundException(Personne.class, idPersonne));
        Long specialite = p.getCategorieSpecialite() == null ? null : p.getCategorieSpecialite().getId();
        if (specialite == null || !specialite.equals(idCategorie)) {
            throw new BusinessException("Le fournisseur « " + p.getNomComplet()
                    + " » n'est pas spécialisé dans cette catégorie de services.");
        }
        Long racineCible = idServiceCourant == null ? null : racineDe(idServiceCourant);
        boolean gereDeja = serviceRepo.findByProprietaireId(idPersonne).stream()
                .anyMatch(autre -> {
                    Long racineAutre = racineDe(autre.getId());
                    return racineCible == null || !racineCible.equals(racineAutre);
                });
        if (gereDeja) {
            throw new BusinessException("Le fournisseur « " + p.getNomComplet()
                    + " » gère déjà un service. Un fournisseur ne gère qu'un seul service.");
        }
        return p;
    }

    /** Remonte l'arborescence jusqu'au service racine. */
    private Long racineDe(Long idService) {
        Service s = serviceRepo.findWithParentsById(idService).orElse(null);
        int garde = 0;
        while (s != null && s.getParent() != null && garde++ < 100) {
            s = s.getParent();
        }
        return s == null ? idService : s.getId();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_SUPPRIMER)")
    public void basculerService(Long id) {
        Service s = serviceRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Service.class, id));
        exigerPerimetreService(s);
        if (s.isEtat() && (serviceRepo.countByParentId(id) > 0 || catalogueRepo.countByServiceId(id) > 0)) {
            throw new BusinessException("Ce service a des sous-services ou des catalogues actifs : désactivez-les d'abord.");
        }
        s.setEtat(!s.isEtat());
    }

    // ==================================================== Catalogue ==========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public Page<CatalogueDto> rechercherCatalogues(String filtre, Pageable pageable) {
        return catalogueRepo.findAll(etendre(texteSpec(filtre, "libelle", "description"), perimetreCatalogueSpec()), pageable)
                .map(catalogueMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<CatalogueDto> cataloguesActifs() {
        Set<Long> autorises = perimetre.servicesAutorises();
        return catalogueRepo.findByEtatTrueOrderByLibelleAsc().stream()
                .filter(c -> autorises == null
                        || (c.getService() != null && autorises.contains(c.getService().getId())))
                .map(catalogueMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<CatalogueDto> cataloguesParService(Long idService) {
        if (idService == null) {
            return List.of();
        }
        return catalogueRepo.findByServiceIdAndEtatTrueOrderByLibelleAsc(idService)
                .stream().map(catalogueMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_CREER)")
    public CatalogueDto creerCatalogue(CatalogueDto dto) {
        Service service = chargerService(dto.getServiceId());
        exigerPerimetreService(service);
        Catalogue c = catalogueMapper.toEntity(dto);
        c.setService(service);
        return catalogueMapper.toDto(catalogueRepo.save(c));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public CatalogueDto modifierCatalogue(Long id, CatalogueDto dto) {
        Catalogue c = catalogueRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Catalogue.class, id));
        exigerPerimetreService(c.getService());
        Service service = chargerService(dto.getServiceId());
        exigerPerimetreService(service);
        catalogueMapper.update(c, dto);
        c.setService(service);
        return catalogueMapper.toDto(c);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_SUPPRIMER)")
    public void basculerCatalogue(Long id) {
        Catalogue c = catalogueRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Catalogue.class, id));
        exigerPerimetreService(c.getService());
        if (c.isEtat() && produitRepo.countByCatalogueId(id) > 0) {
            throw new BusinessException("Ce catalogue contient des produits : désactivez-les d'abord.");
        }
        c.setEtat(!c.isEtat());
    }

    // ==================================================== Produit ============

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public Page<ProduitDto> rechercherProduits(String filtre, Pageable pageable) {
        return produitRepo.findAll(etendre(texteSpec(filtre, "libelle", "description"), perimetreProduitSpec()), pageable)
                .map(produitMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<ProduitDto> produitsActifs() {
        Set<Long> autorises = perimetre.servicesAutorises();
        return produitRepo.findByEtatTrueOrderByLibelleAsc().stream()
                .filter(p -> autorises == null || (p.getCatalogue() != null
                        && p.getCatalogue().getService() != null
                        && autorises.contains(p.getCatalogue().getService().getId())))
                .map(produitMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public ProduitDto produit(Long id) {
        return produitMapper.toDto(produitRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Produit.class, id)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_CREER)")
    public ProduitDto creerProduit(ProduitDto dto) {
        Catalogue catalogue = chargerCatalogue(dto.getCatalogueId());
        exigerPerimetreService(catalogue.getService());
        Produit p = produitMapper.toEntity(dto);
        p.setCatalogue(catalogue);
        Produit saved = produitRepo.save(p);
        if (dto.getProprieteIds() != null && !dto.getProprieteIds().isEmpty()) {
            reconcilierProprietes(saved, dto.getProprieteIds());
        }
        return produitMapper.toDto(saved);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public ProduitDto modifierProduit(Long id, ProduitDto dto) {
        Produit p = produitRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Produit.class, id));
        exigerPerimetreService(p.getCatalogue() != null ? p.getCatalogue().getService() : null);
        Catalogue catalogue = chargerCatalogue(dto.getCatalogueId());
        exigerPerimetreService(catalogue.getService());
        produitMapper.update(p, dto);
        p.setCatalogue(catalogue);
        return produitMapper.toDto(p);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_SUPPRIMER)")
    public void basculerProduit(Long id) {
        Produit p = chargerProduitAvecService(id);
        exigerPerimetreProduit(p);
        if (p.isEtat() && articleRepo.countByProduitId(id) > 0) {
            throw new BusinessException("Ce produit a des articles actifs : désactivez-les d'abord.");
        }
        p.setEtat(!p.isEtat());
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public ProduitDto affecterProprietes(Long idProduit, Set<Long> proprieteIds) {
        Produit p = produitRepo.findWithDetailsById(idProduit)
                .orElseThrow(() -> new ResourceNotFoundException(Produit.class, idProduit));
        exigerPerimetreProduit(p);
        reconcilierProprietes(p, proprieteIds == null ? Set.of() : proprieteIds);
        return produitMapper.toDto(p);
    }

    // ==================================================== Article ============

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public Page<ArticleDto> rechercherArticles(String filtre, Pageable pageable) {
        return articleRepo.findAll(texteSpec(filtre, "reference", "description"), pageable)
                .map(articleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public ArticleDto article(Long id) {
        return articleMapper.toDto(articleRepo.findWithVarietesById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Article.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<ArticleDto> articlesParProduit(Long idProduit) {
        return articleRepo.findByProduitIdOrderByReferenceAsc(idProduit)
                .stream().map(articleMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_CREER)")
    public ArticleDto creerArticle(ArticleDto dto) {
        if (StringUtils.hasText(dto.getReference()) && articleRepo.existsByReferenceIgnoreCase(dto.getReference())) {
            throw new BusinessException("La référence « " + dto.getReference() + " » est déjà utilisée.");
        }
        controlerRemise(dto);
        Produit produit = chargerProduitAvecService(dto.getProduitId());
        exigerPerimetreProduit(produit);
        Article a = articleMapper.toEntity(dto);
        a.setProduit(produit);
        return articleMapper.toDto(articleRepo.save(a));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public ArticleDto modifierArticle(Long id, ArticleDto dto) {
        Article a = articleRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Article.class, id));
        if (StringUtils.hasText(dto.getReference())) {
            articleRepo.findByReferenceIgnoreCase(dto.getReference())
                    .filter(autre -> !autre.getId().equals(id))
                    .ifPresent(autre -> {
                        throw new BusinessException("La référence « " + dto.getReference() + " » est déjà utilisée.");
                    });
        }
        controlerRemise(dto);
        if (a.getProduit() != null) {
            exigerPerimetreProduit(chargerProduitAvecService(a.getProduit().getId()));
        }
        Produit produit = chargerProduitAvecService(dto.getProduitId());
        exigerPerimetreProduit(produit);
        articleMapper.update(a, dto);
        a.setProduit(produit);
        return articleMapper.toDto(a);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_SUPPRIMER)")
    public void basculerArticle(Long id) {
        Article a = articleRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Article.class, id));
        if (a.getProduit() != null) {
            exigerPerimetreProduit(chargerProduitAvecService(a.getProduit().getId()));
        }
        a.setEtat(!a.isEtat());
    }

    // ==================================================== VarieteArticle =====

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<VarieteArticleDto> varietesParArticle(Long idArticle) {
        return varieteRepo.findByArticleIdOrderByProprietesArticleLibelleAsc(idArticle)
                .stream().map(varieteMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<VarieteArticleDto> grilleVarietes(Long idArticle) {
        Article a = articleRepo.findWithVarietesById(idArticle)
                .orElseThrow(() -> new ResourceNotFoundException(Article.class, idArticle));
        Produit p = produitRepo.findWithDetailsById(a.getProduit().getId())
                .orElseThrow(() -> new ResourceNotFoundException(Produit.class, a.getProduit().getId()));

        Map<Long, VarieteArticle> saisies = new LinkedHashMap<>();
        a.getVarietes().forEach(v -> saisies.put(v.getProprietesArticle().getId(), v));

        List<VarieteArticleDto> grille = new ArrayList<>();
        for (ProprietesArticle prop : p.getProprietes()) {
            VarieteArticle existante = saisies.get(prop.getId());
            VarieteArticleDto dto = new VarieteArticleDto();
            dto.setArticleId(a.getId());
            dto.setArticleReference(a.getReference());
            dto.setProprietesArticleId(prop.getId());
            dto.setProprietesArticleLibelle(prop.getLibelle());
            dto.setTypeSaisie(prop.getTypeSaisie());
            dto.setUniteMesureSymbole(prop.getUniteMesure() != null ? prop.getUniteMesure().getSymbole() : null);
            if (existante != null) {
                dto.setId(existante.getId());
                dto.setValeur(existante.getValeur());
                dto.setEtat(existante.isEtat());
            }
            grille.add(dto);
        }
        return grille;
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public VarieteArticleDto enregistrerVariete(Long idArticle, Long idPropriete, String valeur) {
        Article a = articleRepo.findById(idArticle)
                .orElseThrow(() -> new ResourceNotFoundException(Article.class, idArticle));
        if (a.getProduit() != null) {
            exigerPerimetreProduit(chargerProduitAvecService(a.getProduit().getId()));
        }
        ProprietesArticle prop = proprieteRepo.findById(idPropriete)
                .orElseThrow(() -> new ResourceNotFoundException(ProprietesArticle.class, idPropriete));

        VarieteArticle v = varieteRepo.findByArticleIdAndProprietesArticleId(idArticle, idPropriete)
                .orElseGet(() -> {
                    VarieteArticle nouvelle = new VarieteArticle();
                    nouvelle.setArticle(a);
                    nouvelle.setProprietesArticle(prop);
                    return nouvelle;
                });
        v.setValeur(valeur);
        v.setEtat(true);
        return varieteMapper.toDto(varieteRepo.save(v));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public void basculerVariete(Long idVariete) {
        VarieteArticle v = varieteRepo.findById(idVariete)
                .orElseThrow(() -> new ResourceNotFoundException(VarieteArticle.class, idVariete));
        if (v.getArticle() != null && v.getArticle().getProduit() != null) {
            exigerPerimetreProduit(chargerProduitAvecService(v.getArticle().getProduit().getId()));
        }
        v.setEtat(!v.isEtat());
    }

    // ==================================================== Référentiel ========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public Page<ProprietesArticleDto> rechercherProprietes(String filtre, Pageable pageable) {
        return proprieteRepo.findAll(texteSpec(filtre, "libelle", "description", "typeSaisie"), pageable)
                .map(proprieteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<ProprietesArticleDto> proprietesActives() {
        return proprieteRepo.findByEtatTrueOrderByLibelleAsc().stream().map(proprieteMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_CREER)")
    public ProprietesArticleDto creerPropriete(ProprietesArticleDto dto) {
        ProprietesArticle p = proprieteMapper.toEntity(dto);
        p.setUniteMesure(resoudreUnite(dto.getUniteMesureId()));
        return proprieteMapper.toDto(proprieteRepo.save(p));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public ProprietesArticleDto modifierPropriete(Long id, ProprietesArticleDto dto) {
        ProprietesArticle p = proprieteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ProprietesArticle.class, id));
        proprieteMapper.update(p, dto);
        p.setUniteMesure(resoudreUnite(dto.getUniteMesureId()));
        return proprieteMapper.toDto(p);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_SUPPRIMER)")
    public void basculerPropriete(Long id) {
        ProprietesArticle p = proprieteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ProprietesArticle.class, id));
        p.setEtat(!p.isEtat());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public Page<UniteMesureDto> rechercherUnites(String filtre, Pageable pageable) {
        return uniteRepo.findAll(texteSpec(filtre, "libelle", "symbole"), pageable).map(uniteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_LIRE)")
    public List<UniteMesureDto> unitesActives() {
        return uniteRepo.findByEtatTrueOrderByLibelleAsc().stream().map(uniteMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_CREER)")
    public UniteMesureDto creerUnite(UniteMesureDto dto) {
        if (StringUtils.hasText(dto.getSymbole()) && uniteRepo.existsBySymboleIgnoreCase(dto.getSymbole())) {
            throw new BusinessException("Le symbole « " + dto.getSymbole() + " » est déjà utilisé.");
        }
        return uniteMapper.toDto(uniteRepo.save(uniteMapper.toEntity(dto)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_MODIFIER)")
    public UniteMesureDto modifierUnite(Long id, UniteMesureDto dto) {
        UniteMesure u = uniteRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(UniteMesure.class, id));
        if (StringUtils.hasText(dto.getSymbole())) {
            uniteRepo.findBySymboleIgnoreCase(dto.getSymbole())
                    .filter(autre -> !autre.getId().equals(id))
                    .ifPresent(autre -> {
                        throw new BusinessException("Le symbole « " + dto.getSymbole() + " » est déjà utilisé.");
                    });
        }
        uniteMapper.update(u, dto);
        return uniteMapper.toDto(u);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CATALOGUE_SUPPRIMER)")
    public void basculerUnite(Long id) {
        UniteMesure u = uniteRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(UniteMesure.class, id));
        if (u.isEtat() && proprieteRepo.countByUniteMesureId(id) > 0) {
            throw new BusinessException("Cette unité est utilisée par des propriétés : détachez-les d'abord.");
        }
        u.setEtat(!u.isEtat());
    }

    // ==================================================== util ===============

    private CategorieService chargerCategorie(Long id) {
        return categorieRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(CategorieService.class, id));
    }

    private Service chargerService(Long id) {
        return serviceRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Service.class, id));
    }

    private Catalogue chargerCatalogue(Long id) {
        return catalogueRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Catalogue.class, id));
    }

    private Produit chargerProduit(Long id) {
        return produitRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Produit.class, id));
    }

    private UniteMesure resoudreUnite(Long id) {
        return id == null ? null : uniteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(UniteMesure.class, id));
    }

    /**
     * Résout le service parent en interdisant tout cycle : le parent ne peut être
     * ni le service lui-même, ni l'un de ses descendants (arborescence de
     * sous-catégories de profondeur illimitée).
     */
    private Service resoudreParent(Long idParent, Long idCourant) {
        if (idParent == null) {
            return null;
        }
        Service parent = serviceRepo.findById(idParent)
                .orElseThrow(() -> new ResourceNotFoundException(Service.class, idParent));
        if (idCourant != null) {
            Service ancetre = parent;
            int garde = 0;
            while (ancetre != null && garde++ < 100) {
                if (idCourant.equals(ancetre.getId())) {
                    throw new BusinessException(
                            "Un service ne peut pas être placé sous lui-même ou l'un de ses sous-services.");
                }
                ancetre = ancetre.getParent();
            }
        }
        return parent;
    }

    private void reconcilierProprietes(Produit produit, Set<Long> cibles) {
        produit.getProprietes().removeIf(pr -> !cibles.contains(pr.getId()));
        Set<Long> presentes = new java.util.HashSet<>();
        produit.getProprietes().forEach(pr -> presentes.add(pr.getId()));
        for (Long idProp : cibles) {
            if (presentes.contains(idProp)) {
                continue;
            }
            produit.getProprietes().add(proprieteRepo.findById(idProp)
                    .orElseThrow(() -> new ResourceNotFoundException(ProprietesArticle.class, idProp)));
        }
    }

    private void controlerRemise(ArticleDto dto) {
        if (dto.getPrix() == null || dto.getPrix().signum() < 0) {
            throw new BusinessException("Le prix doit être positif ou nul.");
        }
        if (dto.isPromotion()) {
            java.math.BigDecimal taux = dto.getTauxRemisePourcentage();
            if (taux == null || taux.signum() <= 0 || taux.compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new BusinessException("Une promotion exige un taux de remise entre 0 et 100 %.");
            }
        }
    }

    // ---------------------------------------------------------- périmètre -----
    //  Délégué à PerimetreFournisseur : « 1 fournisseur = 1 service » — un
    //  prestataire ne voit / ne gère QUE son service et ses sous-services ;
    //  un administrateur garde la vue globale (servicesAutorises() == null).

    private void exigerPerimetreService(Service s) {
        perimetre.exigerService(s == null ? null : s.getId());
    }

    private void exigerPerimetreProduit(Produit p) {
        Service s = (p != null && p.getCatalogue() != null) ? p.getCatalogue().getService() : null;
        exigerPerimetreService(s);
    }

    private Produit chargerProduitAvecService(Long id) {
        return produitRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Produit.class, id));
    }

    private static <T> Specification<T> etendre(Specification<T> a, Specification<T> b) {
        if (a == null) {
            return b;
        }
        return b == null ? a : a.and(b);
    }

    /** {@code true} si l'utilisateur ne peut pas voir ce service (hors périmètre fournisseur). */
    private boolean horsPerimetreService(Long idService) {
        return !perimetre.couvreService(idService);
    }

    private Specification<CategorieService> perimetreCategorieSpec() {
        Long perim = perimetre.idCategorie();
        return perim == null ? null : (root, q, cb) -> cb.equal(root.get("id"), perim);
    }

    private Specification<Service> perimetreServiceSpec() {
        Set<Long> ids = perimetre.servicesAutorises();
        return ids == null ? null : (root, q, cb) -> root.get("id").in(idsOuNul(ids));
    }

    private Specification<Catalogue> perimetreCatalogueSpec() {
        Set<Long> ids = perimetre.servicesAutorises();
        return ids == null ? null
                : (root, q, cb) -> root.get("service").get("id").in(idsOuNul(ids));
    }

    private Specification<Produit> perimetreProduitSpec() {
        Set<Long> ids = perimetre.servicesAutorises();
        return ids == null ? null
                : (root, q, cb) -> root.get("catalogue").get("service").get("id").in(idsOuNul(ids));
    }

    /** Un {@code IN ()} vide plante côté SQL : on injecte {@code {-1}} pour « aucun résultat ». */
    private static java.util.Collection<Long> idsOuNul(Set<Long> ids) {
        return ids.isEmpty() ? List.of(-1L) : ids;
    }

    private static <T> Specification<T> texteSpec(String filtre, String... champs) {
        if (!StringUtils.hasText(filtre)) {
            return null;
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            List<Predicate> ors = new ArrayList<>();
            for (String champ : champs) {
                ors.add(cb.like(cb.lower(root.get(champ)), motif));
            }
            return cb.or(ors.toArray(Predicate[]::new));
        };
    }
}
