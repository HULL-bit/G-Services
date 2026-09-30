package com.gservices.service.impl;

import com.gservices.dto.ArticleDto;
import com.gservices.dto.CommandeDto;
import com.gservices.dto.LigneCommandeDto;
import com.gservices.entity.*;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.ArticleMapper;
import com.gservices.mapper.CommandeMapper;
import com.gservices.repository.*;
import com.gservices.security.PerimetreFournisseur;
import com.gservices.security.SecurityUtils;
import com.gservices.service.CommandeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@org.springframework.stereotype.Service
@Transactional
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepo;
    private final ServiceRepository serviceRepo;
    private final ArticleRepository articleRepo;
    private final PersonneRepository personneRepo;
    private final CommandeMapper mapper;
    private final ArticleMapper articleMapper;
    private final PerimetreFournisseur perimetre;

    public CommandeServiceImpl(CommandeRepository commandeRepo, ServiceRepository serviceRepo,
                               ArticleRepository articleRepo, PersonneRepository personneRepo,
                               CommandeMapper mapper, ArticleMapper articleMapper,
                               PerimetreFournisseur perimetre) {
        this.commandeRepo = commandeRepo;
        this.serviceRepo = serviceRepo;
        this.articleRepo = articleRepo;
        this.personneRepo = personneRepo;
        this.mapper = mapper;
        this.articleMapper = articleMapper;
        this.perimetre = perimetre;
    }

    // =============================================== tunnel public ==========

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDto> articlesCommandables(Long idService) {
        if (idService == null) {
            return List.of();
        }
        return articleRepo.findCommandablesParService(idService).stream()
                .map(articleMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public CommandeDto passerCommande(CommandeDto dto) {
        Personne client = personneCourant();
        Service service = serviceRepo.findById(dto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(Service.class, dto.getServiceId()));
        if (!service.isCommandeADistance() || !service.isEtat()) {
            throw new BusinessException("Ce service n'accepte pas les commandes à distance.");
        }
        List<LigneCommandeDto> lignesDto = dto.getLignes() == null ? List.of()
                : dto.getLignes().stream().filter(l -> l.getArticleId() != null && l.getQuantite() > 0).toList();
        if (lignesDto.isEmpty()) {
            throw new BusinessException("Votre commande est vide : ajoutez au moins un article.");
        }

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setService(service);
        commande.setStatut(StatutCommande.NOUVELLE);
        commande.setAdresseLivraison(StringUtils.hasText(dto.getAdresseLivraison())
                ? dto.getAdresseLivraison().trim() : null);
        commande.setTelephoneContact(StringUtils.hasText(dto.getTelephoneContact())
                ? dto.getTelephoneContact().trim() : null);
        commande.setCommentaire(StringUtils.hasText(dto.getCommentaire()) ? dto.getCommentaire().trim() : null);

        for (LigneCommandeDto ld : lignesDto) {
            Article article = articleRepo.findWithVarietesById(ld.getArticleId())
                    .orElseThrow(() -> new ResourceNotFoundException(Article.class, ld.getArticleId()));
            Long svcArticle = articleRepo.serviceIdDe(article.getId()).orElse(null);
            if (svcArticle == null || !svcArticle.equals(service.getId())) {
                throw new BusinessException("L'article « " + article.getReference()
                        + " » n'appartient pas à ce service.");
            }
            if (!article.isEtat() || !article.isDisponibilite()) {
                throw new BusinessException("L'article « " + article.getReference() + " » n'est plus disponible.");
            }
            int qte = Math.max(1, ld.getQuantite());
            Stock stock = article.getStock();
            if (stock != null && stock.isEtat() && stock.getQuantiteDisponible() < qte) {
                throw new BusinessException("Stock insuffisant pour « " + article.getReference()
                        + " » (disponible : " + stock.getQuantiteDisponible() + ").");
            }
            LigneCommande ligne = new LigneCommande();
            ligne.setArticle(article);
            ligne.setQuantite(qte);
            ligne.setPrixUnitaire(article.getPrixNet());
            commande.addLigne(ligne);
        }
        commande.recalculerTotal();
        commande.setReference("TMP" + System.nanoTime());   // placeholder unique <= 30 car. (colonne NOT NULL)
        commande = commandeRepo.saveAndFlush(commande);
        commande.setReference(String.format("CMD-%06d", commande.getId())); // écrasé au commit (entité gérée)
        return mapper.toDto(commande);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<CommandeDto> mesCommandes() {
        Long moi = SecurityUtils.currentPersonneId().orElseThrow(
                () -> new BusinessException("Vous devez être connecté."));
        return commandeRepo.findByClientIdAndEtatTrueOrderByDateCommandeDesc(moi).stream()
                .map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public CommandeDto maCommande(Long id) {
        Commande c = charger(id);
        Long moi = SecurityUtils.currentPersonneId().orElse(null);
        if (moi == null || c.getClient() == null || !moi.equals(c.getClient().getId())) {
            throw new BusinessException("Cette commande n'est pas la vôtre.");
        }
        return toDetail(c);
    }

    // =============================================== back-office ============

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).COMMANDE_LIRE)")
    public Page<CommandeDto> rechercherCommandes(String filtre, StatutCommande statut, Pageable pageable) {
        List<Specification<Commande>> specs = new ArrayList<>();
        if (StringUtils.hasText(filtre)) {
            String motif = "%" + filtre.trim().toLowerCase() + "%";
            specs.add((r, q, cb) -> cb.or(
                    cb.like(cb.lower(r.get("reference")), motif),
                    cb.like(cb.lower(r.get("service").get("libelle")), motif),
                    cb.like(cb.lower(r.get("client").get("nom")), motif),
                    cb.like(cb.lower(r.get("client").get("prenom")), motif)));
        }
        if (statut != null) {
            specs.add((r, q, cb) -> cb.equal(r.get("statut"), statut));
        }
        Set<Long> autorises = perimetre.servicesAutorises();
        if (autorises != null) {
            java.util.Collection<Long> ids = autorises.isEmpty() ? List.of(-1L) : autorises;
            specs.add((r, q, cb) -> r.get("service").get("id").in(ids));
        }
        Specification<Commande> spec = specs.isEmpty() ? null
                : specs.stream().reduce(Specification::and).orElse(null);
        return commandeRepo.findAll(spec, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).COMMANDE_LIRE)")
    public CommandeDto commande(Long id) {
        Commande c = charger(id);
        exigerPerimetre(c);
        return toDetail(c);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).COMMANDE_TRAITER)")
    public CommandeDto changerStatut(Long id, StatutCommande cible) {
        Commande c = charger(id);
        exigerPerimetre(c);
        if (cible == null) {
            throw new BusinessException("Statut cible manquant.");
        }
        if (!c.getStatut().estOuverte()) {
            throw new BusinessException("Cette commande est " + libelle(c.getStatut()) + " : elle ne peut plus évoluer.");
        }
        if (!transitionsAutorisees(c.getStatut()).contains(cible)) {
            throw new BusinessException("Transition « " + libelle(c.getStatut()) + " → " + libelle(cible) + " » interdite.");
        }
        c.setStatut(cible);
        return toDetail(c);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).COMMANDE_LIRE)")
    public long nbCommandesATraiter() {
        return rechercherCommandes(null, StatutCommande.NOUVELLE, Pageable.ofSize(1)).getTotalElements();
    }

    // =============================================== util ==================

    private Commande charger(Long id) {
        return commandeRepo.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Commande.class, id));
    }

    private CommandeDto toDetail(Commande c) {
        CommandeDto dto = mapper.toDto(c);
        dto.setLignes(c.getLignes().stream().filter(LigneCommande::isEtat)
                .map(mapper::toDto).toList());
        return dto;
    }

    private Personne personneCourant() {
        Long id = SecurityUtils.currentPersonneId()
                .orElseThrow(() -> new BusinessException("Vous devez être connecté pour commander."));
        return personneRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Personne.class, id));
    }

    private void exigerPerimetre(Commande c) {
        Long idService = c.getService() == null ? null : c.getService().getId();
        if (!perimetre.couvreService(idService)) {
            throw new BusinessException("Cette commande concerne un service hors de votre périmètre.");
        }
    }

    private static Set<StatutCommande> transitionsAutorisees(StatutCommande depuis) {
        return switch (depuis) {
            case NOUVELLE -> EnumSet.of(StatutCommande.CONFIRMEE, StatutCommande.ANNULEE);
            case CONFIRMEE -> EnumSet.of(StatutCommande.EN_PREPARATION, StatutCommande.ANNULEE);
            case EN_PREPARATION -> EnumSet.of(StatutCommande.EXPEDIEE, StatutCommande.ANNULEE);
            case EXPEDIEE -> EnumSet.of(StatutCommande.LIVREE, StatutCommande.ANNULEE);
            default -> EnumSet.noneOf(StatutCommande.class);
        };
    }

    private static String libelle(StatutCommande s) {
        return s.name().toLowerCase().replace('_', ' ');
    }
}
