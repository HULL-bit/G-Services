package com.gservices.service.impl;

import com.gservices.dto.*;
import com.gservices.entity.*;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.*;
import com.gservices.repository.*;
import com.gservices.service.StockService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation du module Stock &amp; prestataires. Règles de gestion appliquées :
 * la quantité disponible et l'alerte de seuil ignorent les lots périmés ou
 * désactivés ({@link Stock#getQuantiteDisponible()}), un lot ne peut pas être
 * antidaté après sa péremption, et un article/service ne peut porter qu'un seul
 * stock / une seule fiche prestataire.
 */
@org.springframework.stereotype.Service
@Transactional
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepo;
    private final LotRepository lotRepo;
    private final InformationServiceRepository infoRepo;
    private final HoraireRepository horaireRepo;
    private final ArticleRepository articleRepo;
    private final ServiceRepository serviceRepo;
    private final PositionRepository positionRepo;
    private final com.gservices.repository.VilleRepository villeRepo;
    private final AnneeRepository anneeRepo;
    private final MoisRepository moisRepo;
    private final JourRepository jourRepo;

    private final StockMapper stockMapper;
    private final LotMapper lotMapper;
    private final InformationServiceMapper infoMapper;
    private final HoraireMapper horaireMapper;
    private final ArticleMapper articleMapper;
    private final ServiceMapper serviceMapper;
    private final PositionMapper positionMapper;
    private final AnneeMapper anneeMapper;
    private final MoisMapper moisMapper;
    private final JourMapper jourMapper;
    private final com.gservices.security.PerimetreFournisseur perimetre;

    public StockServiceImpl(StockRepository stockRepo, LotRepository lotRepo,
                            InformationServiceRepository infoRepo, HoraireRepository horaireRepo,
                            ArticleRepository articleRepo, ServiceRepository serviceRepo,
                            PositionRepository positionRepo, com.gservices.repository.VilleRepository villeRepo,
                            AnneeRepository anneeRepo,
                            MoisRepository moisRepo, JourRepository jourRepo,
                            com.gservices.security.PerimetreFournisseur perimetre,
                            StockMapper stockMapper, LotMapper lotMapper,
                            InformationServiceMapper infoMapper, HoraireMapper horaireMapper,
                            ArticleMapper articleMapper, ServiceMapper serviceMapper,
                            PositionMapper positionMapper, AnneeMapper anneeMapper,
                            MoisMapper moisMapper, JourMapper jourMapper) {
        this.perimetre = perimetre;
        this.stockRepo = stockRepo;
        this.lotRepo = lotRepo;
        this.infoRepo = infoRepo;
        this.horaireRepo = horaireRepo;
        this.articleRepo = articleRepo;
        this.serviceRepo = serviceRepo;
        this.positionRepo = positionRepo;
        this.villeRepo = villeRepo;
        this.anneeRepo = anneeRepo;
        this.moisRepo = moisRepo;
        this.jourRepo = jourRepo;
        this.stockMapper = stockMapper;
        this.lotMapper = lotMapper;
        this.infoMapper = infoMapper;
        this.horaireMapper = horaireMapper;
        this.articleMapper = articleMapper;
        this.serviceMapper = serviceMapper;
        this.positionMapper = positionMapper;
        this.anneeMapper = anneeMapper;
        this.moisMapper = moisMapper;
        this.jourMapper = jourMapper;
    }

    // ==================================================== Stock ==============

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public Page<StockDto> rechercherStocks(String filtre, Long idFournisseur, Pageable pageable) {
        Specification<Stock> spec = null;
        if (StringUtils.hasText(filtre)) {
            String motif = "%" + filtre.trim().toLowerCase() + "%";
            spec = (root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("article").get("reference")), motif),
                    cb.like(cb.lower(root.get("article").get("produit").get("catalogue").get("service").get("libelle")), motif));
        }
        if (idFournisseur != null) {
            Specification<Stock> frn = (root, q, cb) -> cb.equal(
                    root.get("article").get("produit").get("catalogue").get("service").get("proprietaire").get("id"),
                    idFournisseur);
            spec = spec == null ? frn : spec.and(frn);
        }
        java.util.Set<Long> autorises = perimetre.servicesAutorises();
        if (autorises != null) {
            java.util.Collection<Long> ids = autorises.isEmpty() ? java.util.List.of(-1L) : autorises;
            Specification<Stock> perimSpec = (root, q, cb) ->
                    root.get("article").get("produit").get("catalogue").get("service").get("id").in(ids);
            spec = spec == null ? perimSpec : spec.and(perimSpec);
        }
        return stockRepo.findAll(spec, pageable).map(stockMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<PersonneDto> fournisseursAvecStock() {
        java.util.Map<Long, PersonneDto> parId = new java.util.LinkedHashMap<>();
        for (Stock s : stockRepo.findAll()) {
            com.gservices.entity.Personne prop = s.getArticle() != null
                    && s.getArticle().getProduit() != null
                    && s.getArticle().getProduit().getCatalogue() != null
                    && s.getArticle().getProduit().getCatalogue().getService() != null
                    ? s.getArticle().getProduit().getCatalogue().getService().getProprietaire() : null;
            if (prop != null && !parId.containsKey(prop.getId())) {
                PersonneDto d = new PersonneDto();
                d.setId(prop.getId());
                d.setNom(prop.getNom());
                d.setPrenom(prop.getPrenom());
                d.setLogin(prop.getLogin());
                parId.put(prop.getId(), d);
            }
        }
        return parId.values().stream()
                .sorted(java.util.Comparator.comparing(PersonneDto::getNom, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PersonneDto::getPrenom, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public StockDto stock(Long id) {
        return stockMapper.toDto(stockRepo.findWithLotsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Stock.class, id)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_CREER)")
    public StockDto ouvrirStock(StockDto dto) {
        if (dto.getArticleId() == null) {
            throw new BusinessException("Un stock doit être rattaché à un article.");
        }
        if (stockRepo.existsByArticleId(dto.getArticleId())) {
            throw new BusinessException("Cet article possède déjà un stock.");
        }
        Article article = articleRepo.findById(dto.getArticleId())
                .orElseThrow(() -> new ResourceNotFoundException(Article.class, dto.getArticleId()));
        exigerPerimetreArticle(article);
        Stock stock = stockMapper.toEntity(dto);
        stock.setArticle(article);
        return stockMapper.toDto(stockRepo.save(stock));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_MODIFIER)")
    public StockDto modifierStock(Long id, StockDto dto) {
        Stock s = stockRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Stock.class, id));
        exigerPerimetreStock(s);
        s.setQuantiteTotale(Math.max(0, dto.getQuantiteTotale()));
        s.setSeuilAlerte(Math.max(0, dto.getSeuilAlerte()));
        return stockMapper.toDto(s);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_SUPPRIMER)")
    public void basculerStock(Long id) {
        Stock s = stockRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Stock.class, id));
        exigerPerimetreStock(s);
        s.setEtat(!s.isEtat());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<ArticleDto> articlesSansStock() {
        java.util.Set<Long> autorises = perimetre.servicesAutorises();
        return articleRepo.findByStockIsNullAndEtatTrueOrderByReferenceAsc().stream()
                .filter(a -> autorises == null || autorises.contains(serviceDeArticle(a)))
                .map(articleMapper::toDto).toList();
    }

    private Long serviceDeArticle(Article a) {
        if (a.getProduit() == null || a.getProduit().getCatalogue() == null
                || a.getProduit().getCatalogue().getService() == null) {
            return null;
        }
        return a.getProduit().getCatalogue().getService().getId();
    }

    private void exigerPerimetreArticle(Article a) {
        perimetre.exigerService(serviceDeArticle(a));
    }

    private void exigerPerimetreStock(Stock s) {
        exigerPerimetreArticle(s.getArticle());
    }

    private void exigerPerimetreService(Service s) {
        perimetre.exigerService(s == null ? null : s.getId());
    }

    // ==================================================== Lot ================

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<LotDto> lotsParStock(Long idStock) {
        return lotRepo.findByStockIdOrderByDatePeremptionAsc(idStock)
                .stream().map(lotMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_MODIFIER)")
    public LotDto enregistrerLot(Long idStock, LotDto dto) {
        Stock stock = stockRepo.findById(idStock)
                .orElseThrow(() -> new ResourceNotFoundException(Stock.class, idStock));
        exigerPerimetreStock(stock);
        Mois mois = moisRepo.findById(dto.getMoisId())
                .orElseThrow(() -> new ResourceNotFoundException(Mois.class, dto.getMoisId()));
        controlerLot(dto);

        Lot lot;
        if (dto.getId() != null) {
            lot = lotRepo.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(Lot.class, dto.getId()));
            lotMapper.update(lot, dto);
        } else {
            lot = lotMapper.toEntity(dto);
            lot.setStock(stock);
        }
        lot.setMois(mois);
        Lot saved = lotRepo.save(lot);
        rafraichirQuantiteTotale(stock);
        return lotMapper.toDto(saved);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_SUPPRIMER)")
    public void basculerLot(Long idLot) {
        Lot lot = lotRepo.findById(idLot).orElseThrow(() -> new ResourceNotFoundException(Lot.class, idLot));
        exigerPerimetreStock(lot.getStock());
        lot.setEtat(!lot.isEtat());
        rafraichirQuantiteTotale(lot.getStock());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<AnneeDto> anneesActives() {
        return anneeRepo.findAllByOrderByValeurAnneeDesc().stream()
                .filter(Annee::isEtat).map(anneeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<MoisDto> moisParAnnee(Long idAnnee) {
        if (idAnnee == null) {
            return List.of();
        }
        return moisRepo.findByAnneeIdOrderByNumeroMoisAsc(idAnnee).stream().map(moisMapper::toDto).toList();
    }

    // ==================================================== InformationService =

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public Page<InformationServiceDto> rechercherPrestataires(String filtre, Pageable pageable) {
        Specification<InformationService> spec = StringUtils.hasText(filtre)
                ? (root, q, cb) -> {
                    String motif = "%" + filtre.trim().toLowerCase() + "%";
                    List<Predicate> ors = new ArrayList<>();
                    ors.add(cb.like(cb.lower(root.get("service").get("libelle")), motif));
                    ors.add(cb.like(cb.lower(root.get("adresse")), motif));
                    ors.add(cb.like(cb.lower(root.get("email1")), motif));
                    return cb.or(ors.toArray(Predicate[]::new));
                }
                : null;
        java.util.Set<Long> autorises = perimetre.servicesAutorises();
        if (autorises != null) {
            java.util.Collection<Long> ids = autorises.isEmpty() ? java.util.List.of(-1L) : autorises;
            Specification<InformationService> perimSpec = (root, q, cb) -> root.get("service").get("id").in(ids);
            spec = spec == null ? perimSpec : spec.and(perimSpec);
        }
        return infoRepo.findAll(spec, pageable).map(infoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public InformationServiceDto prestataire(Long id) {
        return infoMapper.toDto(infoRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(InformationService.class, id)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_CREER)")
    public InformationServiceDto creerPrestataire(InformationServiceDto dto) {
        if (dto.getServiceId() == null) {
            throw new BusinessException("Une fiche prestataire doit être rattachée à un service.");
        }
        if (infoRepo.existsByServiceId(dto.getServiceId())) {
            throw new BusinessException("Ce service possède déjà une fiche prestataire.");
        }
        Service svc = serviceRepo.findById(dto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(Service.class, dto.getServiceId()));
        exigerPerimetreService(svc);
        InformationService info = infoMapper.toEntity(dto);
        info.setService(svc);
        info.setPosition(resoudrePositionFiche(dto, null));
        return infoMapper.toDto(infoRepo.save(info));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_MODIFIER)")
    public InformationServiceDto modifierPrestataire(Long id, InformationServiceDto dto) {
        InformationService info = infoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(InformationService.class, id));
        exigerPerimetreService(info.getService());
        infoMapper.update(info, dto);
        info.setPosition(resoudrePositionFiche(dto, info.getPosition()));
        return infoMapper.toDto(info);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_SUPPRIMER)")
    public void basculerPrestataire(Long id) {
        InformationService info = infoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(InformationService.class, id));
        exigerPerimetreService(info.getService());
        info.setEtat(!info.isEtat());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<ServiceDto> servicesSansFiche() {
        java.util.Set<Long> autorises = perimetre.servicesAutorises();
        return serviceRepo.findByInformationServiceIsNullAndEtatTrueOrderByLibelleAsc().stream()
                .filter(s -> autorises == null || autorises.contains(s.getId()))
                .map(serviceMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<PositionDto> positionsActives() {
        return positionRepo.findAll((r, q, cb) -> cb.isTrue(r.get("etat")))
                .stream().map(positionMapper::toDto).toList();
    }

    // ==================================================== Horaire ===========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<HoraireDto> horairesParPrestataire(Long idPrestataire) {
        return horaireRepo.findByInformationServiceIdOrderByJourNumeroJourSemaineAsc(idPrestataire)
                .stream().map(horaireMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_MODIFIER)")
    public HoraireDto enregistrerHoraire(Long idPrestataire, HoraireDto dto) {
        InformationService info = infoRepo.findById(idPrestataire)
                .orElseThrow(() -> new ResourceNotFoundException(InformationService.class, idPrestataire));
        exigerPerimetreService(info.getService());
        Jour jour = jourRepo.findById(dto.getJourId())
                .orElseThrow(() -> new ResourceNotFoundException(Jour.class, dto.getJourId()));
        controlerHoraire(dto);

        Horaire h = horaireRepo.findByInformationServiceIdAndJourId(idPrestataire, dto.getJourId())
                .orElseGet(() -> {
                    Horaire n = new Horaire();
                    n.setInformationService(info);
                    n.setJour(jour);
                    return n;
                });
        h.setHeureOuverture(dto.getHeureOuverture());
        h.setHeureFermeture(dto.getHeureFermeture());
        h.setPauseDejeunerDebut(dto.getPauseDejeunerDebut());
        h.setPauseDejeunerFin(dto.getPauseDejeunerFin());
        h.setOuvert24h(dto.isOuvert24h());
        h.setEtat(true);
        return horaireMapper.toDto(horaireRepo.save(h));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_SUPPRIMER)")
    public void basculerHoraire(Long idHoraire) {
        Horaire h = horaireRepo.findById(idHoraire)
                .orElseThrow(() -> new ResourceNotFoundException(Horaire.class, idHoraire));
        if (h.getInformationService() != null) {
            exigerPerimetreService(h.getInformationService().getService());
        }
        h.setEtat(!h.isEtat());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).STOCK_LIRE)")
    public List<JourDto> jours() {
        return jourRepo.findAllByOrderByNumeroJourSemaineAsc().stream().map(jourMapper::toDto).toList();
    }

    // ==================================================== util ==============

    private Position resoudrePosition(Long idPosition) {
        return idPosition == null ? null : positionRepo.findById(idPosition)
                .orElseThrow(() -> new ResourceNotFoundException(Position.class, idPosition));
    }

    /**
     * Position d'une fiche prestataire depuis le formulaire : si des coordonnées
     * GPS sont saisies (pointeur sur la carte), on crée / met à jour un point ;
     * sinon on retombe sur une position existante choisie dans la liste.
     */
    private Position resoudrePositionFiche(InformationServiceDto dto, Position actuelle) {
        Double lat = dto.getLatitude();
        Double lng = dto.getLongitude();
        if (lat != null && lng != null && !(lat == 0d && lng == 0d)) {
            Position p = actuelle != null ? actuelle : new Position();
            p.setLatitude(lat);
            p.setLongitude(lng);
            if (p.getDateReleve() == null) {
                p.setDateReleve(java.time.LocalDate.now());
            }
            if (p.getVille() == null) {
                p.setVille(villeRepo.findFirstByLibelleIgnoreCase("Dakar")
                        .or(() -> villeRepo.findAll().stream().findFirst())
                        .orElseThrow(() -> new BusinessException(
                                "Aucune ville de référence : créez d'abord une ville dans Géographie.")));
            }
            return positionRepo.save(p);
        }
        return resoudrePosition(dto.getPositionId());
    }

    private void rafraichirQuantiteTotale(Stock stock) {
        Stock frais = stockRepo.findWithLotsById(stock.getId()).orElse(stock);
        frais.setQuantiteTotale(frais.getQuantiteDisponible());
    }

    private void controlerLot(LotDto dto) {
        if (dto.getQuantite() < 0) {
            throw new BusinessException("La quantité d'un lot ne peut pas être négative.");
        }
        if (dto.getDateEntree() != null && dto.getDatePeremption() != null
                && dto.getDatePeremption().isBefore(dto.getDateEntree())) {
            throw new BusinessException("La date de péremption précède la date d'entrée.");
        }
    }

    private void controlerHoraire(HoraireDto dto) {
        if (dto.isOuvert24h()) {
            return;
        }
        if (StringUtils.hasText(dto.getHeureOuverture()) && StringUtils.hasText(dto.getHeureFermeture())
                && dto.getHeureOuverture().compareTo(dto.getHeureFermeture()) >= 0) {
            throw new BusinessException("L'heure d'ouverture doit précéder l'heure de fermeture.");
        }
    }
}
