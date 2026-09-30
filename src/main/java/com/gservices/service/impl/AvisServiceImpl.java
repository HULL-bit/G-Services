package com.gservices.service.impl;

import com.gservices.dto.AvisDto;
import com.gservices.dto.SyntheseAvisDto;
import com.gservices.entity.*;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.AvisMapper;
import com.gservices.repository.*;
import com.gservices.security.SecurityUtils;
import com.gservices.service.AvisService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class AvisServiceImpl implements AvisService {

    private final AvisRepository avisRepo;
    private final PersonneRepository personneRepo;
    private final ServiceRepository serviceRepo;
    private final ProduitRepository produitRepo;
    private final AnneeRepository anneeRepo;
    private final AvisMapper mapper;
    private final com.gservices.security.PerimetreFournisseur perimetre;

    public AvisServiceImpl(AvisRepository avisRepo, PersonneRepository personneRepo,
                           ServiceRepository serviceRepo, ProduitRepository produitRepo,
                           AnneeRepository anneeRepo, AvisMapper mapper,
                           com.gservices.security.PerimetreFournisseur perimetre) {
        this.avisRepo = avisRepo;
        this.personneRepo = personneRepo;
        this.serviceRepo = serviceRepo;
        this.produitRepo = produitRepo;
        this.anneeRepo = anneeRepo;
        this.mapper = mapper;
        this.perimetre = perimetre;
    }

    // ==================================================== back-office ========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).AVIS_LIRE)")
    public Page<AvisDto> rechercher(String filtre, EtatModeration etat, Pageable pageable) {
        return avisRepo.findAll(specAvecPerimetre(filtre, etat), pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).AVIS_LIRE)")
    public long nombreEnAttente() {
        return avisRepo.count(specAvecPerimetre(null, EtatModeration.EN_ATTENTE));
    }

    private Specification<Avis> specAvecPerimetre(String filtre, EtatModeration etat) {
        Specification<Avis> spec = spec(filtre, etat);
        java.util.Set<Long> autorises = perimetre.servicesAutorises();
        if (autorises != null) {
            java.util.Collection<Long> ids = autorises.isEmpty() ? List.of(-1L) : autorises;
            Specification<Avis> perimSpec = (root, query, cb) -> {
                // jointures explicites en LEFT : un avis ne porte que sur l'UNE des deux
                // cibles (service OU produit) — une jointure implicite (INNER) sur l'autre
                // ferait disparaître ces lignes de la requête entière, pas juste du OR.
                var produit = root.join("produit", jakarta.persistence.criteria.JoinType.LEFT);
                var catalogue = produit.join("catalogue", jakarta.persistence.criteria.JoinType.LEFT);
                var serviceDuProduit = catalogue.join("service", jakarta.persistence.criteria.JoinType.LEFT);
                return cb.or(
                        root.get("service").get("id").in(ids),
                        serviceDuProduit.get("id").in(ids));
            };
            spec = spec == null ? perimSpec : spec.and(perimSpec);
        }
        return spec;
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).AVIS_MODERER)")
    public void approuver(Long idAvis) {
        Avis a = charger(idAvis);
        a.setEstModere(true);
        a.setEtat(true);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).AVIS_MODERER)")
    public void rejeter(Long idAvis) {
        Avis a = charger(idAvis);
        a.setEstModere(true);
        a.setEtat(false);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).AVIS_REPONDRE)")
    public void repondre(Long idAvis, String reponse) {
        Avis a = charger(idAvis);
        a.setReponsePrestataire(StringUtils.hasText(reponse) ? reponse.trim() : null);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).AVIS_SUPPRIMER)")
    public void basculerEtat(Long idAvis) {
        Avis a = charger(idAvis);
        a.setEtat(!a.isEtat());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void repondreProprietaire(Long idAvis, String reponse) {
        Avis a = charger(idAvis);
        Long moi = SecurityUtils.currentPersonneId().orElse(null);
        boolean proprio = a.getService() != null && a.getService().getProprietaire() != null
                && a.getService().getProprietaire().getId().equals(moi);
        boolean moderateur = SecurityUtils.currentAuthorities()
                .contains(com.gservices.security.Perms.AVIS_REPONDRE);
        if (!proprio && !moderateur) {
            throw new BusinessException("Vous ne pouvez répondre qu'aux avis de vos propres services.");
        }
        a.setReponsePrestataire(StringUtils.hasText(reponse) ? reponse.trim() : null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estProprietaireDuService(Long idService) {
        Long moi = SecurityUtils.currentPersonneId().orElse(null);
        if (moi == null || idService == null) {
            return false;
        }
        return serviceRepo.findById(idService)
                .map(s -> s.getProprietaire() != null && s.getProprietaire().getId().equals(moi))
                .orElse(false);
    }

    // ==================================================== public ============

    @Override
    @Transactional(readOnly = true)
    public List<AvisDto> avisPublicsService(Long idService) {
        return avisRepo.findByServiceIdAndEstModereTrueAndEtatTrueOrderByDateDesc(idService)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisDto> avisPublicsProduit(Long idProduit) {
        return avisRepo.findByProduitIdAndEstModereTrueAndEtatTrueOrderByDateDesc(idProduit)
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SyntheseAvisDto syntheseService(Long idService) {
        return synthese(avisRepo.moyenneService(idService), avisRepo.repartitionService(idService));
    }

    @Override
    @Transactional(readOnly = true)
    public SyntheseAvisDto syntheseProduit(Long idProduit) {
        return synthese(avisRepo.moyenneProduit(idProduit), avisRepo.repartitionProduit(idProduit));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public AvisDto deposerAvisService(Long idService, int note, String commentaire) {
        Personne moi = personneCourante();
        if (avisRepo.existsByPersonneIdAndServiceId(moi.getId(), idService)) {
            throw new BusinessException("Vous avez déjà laissé un avis sur ce prestataire.");
        }
        Service service = serviceRepo.findById(idService)
                .orElseThrow(() -> new ResourceNotFoundException(Service.class, idService));
        Avis avis = nouvelAvis(moi, note, commentaire);
        avis.setService(service);
        return mapper.toDto(avisRepo.save(avis));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public AvisDto deposerAvisProduit(Long idProduit, int note, String commentaire) {
        Personne moi = personneCourante();
        if (avisRepo.existsByPersonneIdAndProduitId(moi.getId(), idProduit)) {
            throw new BusinessException("Vous avez déjà laissé un avis sur ce produit.");
        }
        Produit produit = produitRepo.findById(idProduit)
                .orElseThrow(() -> new ResourceNotFoundException(Produit.class, idProduit));
        Avis avis = nouvelAvis(moi, note, commentaire);
        avis.setProduit(produit);
        return mapper.toDto(avisRepo.save(avis));
    }

    @Override
    @Transactional(readOnly = true)
    public AvisDto monAvisService(Long idService) {
        return SecurityUtils.currentPersonneId()
                .flatMap(id -> avisRepo.findAll(
                        (r, q, cb) -> cb.and(cb.equal(r.get("personne").get("id"), id),
                                cb.equal(r.get("service").get("id"), idService))).stream().findFirst())
                .map(mapper::toDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisDto monAvisProduit(Long idProduit) {
        return SecurityUtils.currentPersonneId()
                .flatMap(id -> avisRepo.findAll(
                        (r, q, cb) -> cb.and(cb.equal(r.get("personne").get("id"), id),
                                cb.equal(r.get("produit").get("id"), idProduit))).stream().findFirst())
                .map(mapper::toDto).orElse(null);
    }

    // ==================================================== util ==============

    private Avis charger(Long id) {
        return avisRepo.findDetailById(id).orElseThrow(() -> new ResourceNotFoundException(Avis.class, id));
    }

    private Personne personneCourante() {
        Long id = SecurityUtils.currentPersonneId()
                .orElseThrow(() -> new BusinessException("Vous devez être connecté pour laisser un avis."));
        return personneRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Personne.class, id));
    }

    private Avis nouvelAvis(Personne auteur, int note, String commentaire) {
        if (note < 1 || note > 5) {
            throw new BusinessException("La note doit être comprise entre 1 et 5.");
        }
        Annee annee = anneeRepo.findByValeurAnnee(Year.now().getValue())
                .or(() -> anneeRepo.findAllByOrderByValeurAnneeDesc().stream().findFirst())
                .orElseThrow(() -> new BusinessException("Aucune année de référence n'est configurée."));
        Avis avis = new Avis();
        avis.setPersonne(auteur);
        avis.setAnnee(annee);
        avis.setNote(note);
        avis.setCommentaire(StringUtils.hasText(commentaire) ? commentaire.trim() : null);
        avis.setEstModere(false);
        avis.setEtat(true);
        return avis;
    }

    private static SyntheseAvisDto synthese(double moyenne, List<Object[]> repartition) {
        SyntheseAvisDto s = new SyntheseAvisDto();
        s.setMoyenne(moyenne);
        long total = 0;
        for (Object[] ligne : repartition) {
            int note = ((Number) ligne[0]).intValue();
            long nb = ((Number) ligne[1]).longValue();
            if (note >= 1 && note <= 5) {
                s.getRepartition()[note - 1] = nb;
                total += nb;
            }
        }
        s.setTotal(total);
        return s;
    }

    private static Specification<Avis> spec(String filtre, EtatModeration etat) {
        return (root, query, cb) -> {
            List<Predicate> ands = new ArrayList<>();
            if (etat == EtatModeration.EN_ATTENTE) {
                ands.add(cb.isFalse(root.get("estModere")));
            } else if (etat == EtatModeration.MODERES) {
                ands.add(cb.isTrue(root.get("estModere")));
            }
            if (StringUtils.hasText(filtre)) {
                String motif = "%" + filtre.trim().toLowerCase() + "%";
                ands.add(cb.or(
                        cb.like(cb.lower(root.get("commentaire")), motif),
                        cb.like(cb.lower(root.get("personne").get("nom")), motif),
                        cb.like(cb.lower(root.get("service").get("libelle")), motif),
                        cb.like(cb.lower(root.get("produit").get("libelle")), motif)));
            }
            return ands.isEmpty() ? cb.conjunction() : cb.and(ands.toArray(Predicate[]::new));
        };
    }
}
