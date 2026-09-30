package com.gservices.service.impl;

import com.gservices.dto.SanctionDto;
import com.gservices.dto.SignalementDto;
import com.gservices.entity.*;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.repository.PersonneRepository;
import com.gservices.repository.SanctionRepository;
import com.gservices.repository.ServiceRepository;
import com.gservices.repository.SignalementRepository;
import com.gservices.security.SecurityUtils;
import com.gservices.service.SignalementService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class SignalementServiceImpl implements SignalementService {

    private final SignalementRepository signalementRepo;
    private final SanctionRepository sanctionRepo;
    private final ServiceRepository serviceRepo;
    private final PersonneRepository personneRepo;
    private final com.gservices.security.PerimetreFournisseur perimetre;

    public SignalementServiceImpl(SignalementRepository signalementRepo, SanctionRepository sanctionRepo,
                                  ServiceRepository serviceRepo, PersonneRepository personneRepo,
                                  com.gservices.security.PerimetreFournisseur perimetre) {
        this.signalementRepo = signalementRepo;
        this.sanctionRepo = sanctionRepo;
        this.serviceRepo = serviceRepo;
        this.personneRepo = personneRepo;
        this.perimetre = perimetre;
    }

    /** Un {@code IN ()} vide plante côté SQL : on injecte {@code {-1}} pour « aucun résultat ». */
    private static java.util.Collection<Long> idsOuNul(java.util.Set<Long> ids) {
        return ids.isEmpty() ? List.of(-1L) : ids;
    }

    // ==================================================== public ==============

    @Override
    @PreAuthorize("isAuthenticated()")
    public void signaler(Long idService, MotifSignalement motif, String description) {
        if (motif == null) {
            throw new BusinessException("Merci de préciser le motif du signalement.");
        }
        Long idPersonne = SecurityUtils.currentPersonneId()
                .orElseThrow(() -> new BusinessException("Connectez-vous pour signaler ce service."));
        Service service = serviceRepo.findById(idService)
                .orElseThrow(() -> new ResourceNotFoundException(Service.class, idService));

        Signalement s = new Signalement();
        s.setPersonne(personneRepo.getReferenceById(idPersonne));
        s.setService(service);
        s.setMotif(motif);
        s.setDescription(description);
        s.setDate(LocalDateTime.now());
        s.setStatut(StatutSignalement.NOUVEAU);
        signalementRepo.save(s);
    }

    // ==================================================== back-office : file ==

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).SIGNALEMENT_LIRE)")
    public Page<SignalementDto> rechercher(String filtre, StatutSignalement statut, Pageable pageable) {
        return signalementRepo.findAll(specSignalement(filtre, statut), pageable).map(this::versDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).SIGNALEMENT_LIRE)")
    public long nombreNouveaux() {
        return signalementRepo.count(specSignalement(null, StatutSignalement.NOUVEAU));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).SIGNALEMENT_GERER)")
    public void rejeter(Long idSignalement) {
        Signalement s = charger(idSignalement);
        perimetre.exigerService(s.getService() == null ? null : s.getService().getId());
        s.setStatut(StatutSignalement.REJETE);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).SIGNALEMENT_GERER)")
    public void sanctionner(Long idSignalement, String motifSanction) {
        if (!StringUtils.hasText(motifSanction)) {
            throw new BusinessException("Le motif de la sanction est obligatoire.");
        }
        Signalement s = charger(idSignalement);
        Service service = s.getService();
        perimetre.exigerService(service == null ? null : service.getId());

        Sanction sanction = new Sanction();
        sanction.setService(service);
        sanction.setMotif(motifSanction.trim());
        sanction.setDateSanction(LocalDateTime.now());
        SecurityUtils.currentPersonneId().ifPresent(id -> sanction.setAdmin(personneRepo.getReferenceById(id)));
        sanctionRepo.save(sanction);

        service.setBloque(true);
        s.setStatut(StatutSignalement.TRAITE);
    }

    // ==================================================== back-office : sanctions

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).SIGNALEMENT_LIRE)")
    public Page<SanctionDto> servicesBloques(String filtre, Pageable pageable) {
        return sanctionRepo.findAll(specSanction(filtre), pageable).map(this::versDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).SIGNALEMENT_LIRE)")
    public long nombreServicesBloques() {
        return sanctionRepo.count(specSanction(null));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).SIGNALEMENT_GERER)")
    public void lever(Long idSanction) {
        Sanction sanction = sanctionRepo.findById(idSanction)
                .orElseThrow(() -> new ResourceNotFoundException(Sanction.class, idSanction));
        perimetre.exigerService(sanction.getService() == null ? null : sanction.getService().getId());
        sanction.setDateLevee(LocalDateTime.now());
        if (sanction.getService() != null) {
            sanction.getService().setBloque(false);
        }
    }

    // ==================================================== util ================

    private Signalement charger(Long id) {
        return signalementRepo.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Signalement.class, id));
    }

    private SignalementDto versDto(Signalement s) {
        SignalementDto dto = new SignalementDto();
        dto.setId(s.getId());
        dto.setMotif(s.getMotif());
        dto.setDescription(s.getDescription());
        dto.setDate(s.getDate());
        dto.setStatut(s.getStatut());
        if (s.getPersonne() != null) {
            dto.setPersonneId(s.getPersonne().getId());
            dto.setPersonneNom(s.getPersonne().getNomComplet());
        }
        if (s.getService() != null) {
            dto.setServiceId(s.getService().getId());
            dto.setServiceLibelle(s.getService().getLibelle());
            dto.setServiceBloque(s.getService().isBloque());
            if (s.getService().getCategorieService() != null) {
                dto.setCategorieLibelle(s.getService().getCategorieService().getLibelle());
            }
        }
        return dto;
    }

    private SanctionDto versDto(Sanction s) {
        SanctionDto dto = new SanctionDto();
        dto.setId(s.getId());
        dto.setMotif(s.getMotif());
        dto.setDateSanction(s.getDateSanction());
        dto.setDateLevee(s.getDateLevee());
        dto.setAdminNom(s.getAdmin() != null ? s.getAdmin().getNomComplet() : null);
        if (s.getService() != null) {
            dto.setServiceId(s.getService().getId());
            dto.setServiceLibelle(s.getService().getLibelle());
            if (s.getService().getCategorieService() != null) {
                dto.setCategorieLibelle(s.getService().getCategorieService().getLibelle());
            }
        }
        return dto;
    }

    private static Specification<Signalement> spec(String filtre, StatutSignalement statut) {
        return (root, query, cb) -> {
            List<Predicate> ands = new ArrayList<>();
            if (statut != null) {
                ands.add(cb.equal(root.get("statut"), statut));
            }
            if (StringUtils.hasText(filtre)) {
                String motif = "%" + filtre.trim().toLowerCase() + "%";
                ands.add(cb.or(
                        cb.like(cb.lower(root.get("description")), motif),
                        cb.like(cb.lower(root.get("personne").get("nom")), motif),
                        cb.like(cb.lower(root.get("service").get("libelle")), motif)));
            }
            return ands.isEmpty() ? cb.conjunction() : cb.and(ands.toArray(Predicate[]::new));
        };
    }

    /** Combine le filtre texte/statut avec le périmètre catégorie du gestionnaire (le cas échéant). */
    private Specification<Signalement> specSignalement(String filtre, StatutSignalement statut) {
        Specification<Signalement> s = spec(filtre, statut);
        java.util.Set<Long> autorises = perimetre.servicesAutorises();
        if (autorises != null) {
            java.util.Collection<Long> ids = idsOuNul(autorises);
            Specification<Signalement> perimSpec = (root, query, cb) -> root.get("service").get("id").in(ids);
            s = s == null ? perimSpec : s.and(perimSpec);
        }
        return s;
    }

    private Specification<Sanction> specSanction(String filtre) {
        Specification<Sanction> s = (root, query, cb) -> {
            List<Predicate> ands = new ArrayList<>();
            ands.add(cb.isNull(root.get("dateLevee")));
            if (StringUtils.hasText(filtre)) {
                ands.add(cb.like(cb.lower(root.get("service").get("libelle")),
                        "%" + filtre.trim().toLowerCase() + "%"));
            }
            return cb.and(ands.toArray(Predicate[]::new));
        };
        java.util.Set<Long> autorises = perimetre.servicesAutorises();
        if (autorises != null) {
            java.util.Collection<Long> ids = idsOuNul(autorises);
            Specification<Sanction> perimSpec = (root, query, cb) -> root.get("service").get("id").in(ids);
            s = s.and(perimSpec);
        }
        return s;
    }
}
