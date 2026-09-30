package com.gservices.service.impl;

import com.gservices.dto.CategorieServiceDto;
import com.gservices.dto.CreationCompteDto;
import com.gservices.dto.PersonneDto;
import com.gservices.dto.ServiceDto;
import com.gservices.entity.CategorieService;
import com.gservices.entity.Personne;
import com.gservices.entity.Profil;
import com.gservices.entity.Role;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.CategorieServiceMapper;
import com.gservices.mapper.PersonneMapper;
import com.gservices.mapper.ServiceMapper;
import com.gservices.repository.CategorieServiceRepository;
import com.gservices.repository.PersonneRepository;
import com.gservices.repository.ProfilRepository;
import com.gservices.repository.ServiceRepository;
import com.gservices.security.SecurityUtils;
import com.gservices.service.GestionComptesService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class GestionComptesServiceImpl implements GestionComptesService {

    private static final String PROFIL_FOURNISSEUR = "PRESTATAIRE";
    private static final String PROFIL_CLIENT = "CLIENT";

    private final PersonneRepository personneRepo;
    private final ServiceRepository serviceRepo;
    private final ProfilRepository profilRepo;
    private final CategorieServiceRepository categorieRepo;
    private final PersonneMapper personneMapper;
    private final ServiceMapper serviceMapper;
    private final CategorieServiceMapper categorieMapper;
    private final PasswordEncoder passwordEncoder;

    public GestionComptesServiceImpl(PersonneRepository personneRepo, ServiceRepository serviceRepo,
                                     ProfilRepository profilRepo, CategorieServiceRepository categorieRepo,
                                     PersonneMapper personneMapper, ServiceMapper serviceMapper,
                                     CategorieServiceMapper categorieMapper, PasswordEncoder passwordEncoder) {
        this.personneRepo = personneRepo;
        this.serviceRepo = serviceRepo;
        this.profilRepo = profilRepo;
        this.categorieRepo = categorieRepo;
        this.personneMapper = personneMapper;
        this.serviceMapper = serviceMapper;
        this.categorieMapper = categorieMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================================================== Fournisseurs =======

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).FOURNISSEUR_LIRE)")
    public Page<PersonneDto> rechercherFournisseurs(String filtre, Pageable pageable) {
        return personneRepo.findAll(aProfil(PROFIL_FOURNISSEUR).and(texte(filtre)), pageable)
                .map(this::versDtoFournisseur);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).FOURNISSEUR_GERER)")
    public PersonneDto creerFournisseur(CreationCompteDto dto) {
        if (dto.getCategorieSpecialiteId() == null) {
            throw new BusinessException("Un fournisseur doit être spécialisé dans une catégorie de services.");
        }
        CategorieService specialite = categorieRepo.findById(dto.getCategorieSpecialiteId())
                .orElseThrow(() -> new ResourceNotFoundException(CategorieService.class, dto.getCategorieSpecialiteId()));

        Personne p = creerPersonne(dto, PROFIL_FOURNISSEUR);
        p.setCategorieSpecialite(specialite);
        personneRepo.save(p);

        if (StringUtils.hasText(dto.getServiceLibelle())) {
            com.gservices.entity.Service s = new com.gservices.entity.Service();
            s.setLibelle(dto.getServiceLibelle().trim());
            s.setCategorieService(specialite);
            s.setProprietaire(p);
            s.setValide(true);   // créé par un administrateur : validé d'emblée
            s.setEtat(true);
            serviceRepo.save(s);
        }
        return versDtoFournisseur(p);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).FOURNISSEUR_GERER)")
    public void basculerFournisseur(Long idPersonne) {
        basculer(idPersonne);
    }

    // ==================================================== Clients ============

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CLIENT_LIRE)")
    public Page<PersonneDto> rechercherClients(String filtre, Pageable pageable) {
        return personneRepo.findAll(aProfil(PROFIL_CLIENT).and(texte(filtre)), pageable)
                .map(personneMapper::toDto);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CLIENT_GERER)")
    public PersonneDto creerClient(CreationCompteDto dto) {
        Personne p = creerPersonne(dto, PROFIL_CLIENT);
        personneRepo.save(p);
        return personneMapper.toDto(p);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).CLIENT_GERER)")
    public void basculerClient(Long idPersonne) {
        basculer(idPersonne);
    }

    // ==================================================== Validation ========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).VALIDATION_LIRE)")
    public List<PersonneDto> comptesEnAttente() {
        return personneRepo.findByValideFalseAndEtatTrueOrderByDateInscriptionAsc()
                .stream().map(this::versDtoFournisseur).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).VALIDATION_LIRE)")
    public List<ServiceDto> servicesEnAttente() {
        return serviceRepo.findByValideFalseAndEtatTrueOrderByDateCreationAsc()
                .stream().map(serviceMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).VALIDATION_GERER)")
    public void validerCompte(Long idPersonne) {
        chargerPersonne(idPersonne).setValide(true);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).VALIDATION_GERER)")
    public void refuserCompte(Long idPersonne) {
        Personne p = chargerPersonne(idPersonne);
        p.setValide(false);
        p.setEtat(false);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).VALIDATION_GERER)")
    public void validerService(Long idService) {
        chargerService(idService).setValide(true);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).VALIDATION_GERER)")
    public void refuserService(Long idService) {
        chargerService(idService).setEtat(false);
    }

    // ==================================================== Référentiel =======

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority("
            + "T(com.gservices.security.Perms).FOURNISSEUR_LIRE,"
            + "T(com.gservices.security.Perms).VALIDATION_LIRE)")
    public List<CategorieServiceDto> categoriesReferentiel() {
        return categorieRepo.findByEtatTrueOrderByOrdreAffichageAscLibelleAsc()
                .stream().map(categorieMapper::toDto).toList();
    }

    // ==================================================== util ==============

    private Personne creerPersonne(CreationCompteDto dto, String libelleProfil) {
        if (!StringUtils.hasText(dto.getLogin()) || personneRepo.existsByLoginIgnoreCase(dto.getLogin())) {
            throw new BusinessException("Ce nom d'utilisateur est déjà pris.");
        }
        if (StringUtils.hasText(dto.getEmail()) && personneRepo.existsByEmail1IgnoreCase(dto.getEmail())) {
            throw new BusinessException("Un compte existe déjà avec cet e-mail.");
        }
        Personne p = new Personne();
        p.setNom(dto.getNom().trim());
        p.setPrenom(dto.getPrenom().trim());
        p.setLogin(dto.getLogin().trim());
        p.setEmail1(StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null);
        p.setTelephone1(StringUtils.hasText(dto.getTelephone()) ? dto.getTelephone().trim() : null);
        p.setPassword(passwordEncoder.encode(dto.getMotDePasse()));
        p.setEtat(true);
        p.setValide(true);   // créé par un administrateur

        Profil profil = profilRepo.findByLibelleIgnoreCase(libelleProfil)
                .orElseThrow(() -> new BusinessException("Profil « " + libelleProfil + " » introuvable."));
        Role role = new Role();
        role.setProfil(profil);
        role.setDateAttribution(LocalDateTime.now());
        role.setEtat(true);
        p.addRole(role);
        return p;
    }

    private void basculer(Long idPersonne) {
        Personne p = chargerPersonne(idPersonne);
        SecurityUtils.currentPersonneId()
                .filter(courant -> courant.equals(idPersonne) && p.isEtat())
                .ifPresent(courant -> {
                    throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte.");
                });
        p.setEtat(!p.isEtat());
    }

    private PersonneDto versDtoFournisseur(Personne p) {
        PersonneDto dto = personneMapper.toDto(p);
        serviceRepo.findByProprietaireId(p.getId()).stream()
                .filter(s -> s.getParent() == null)
                .findFirst()
                .ifPresent(s -> dto.setServiceGereLibelle(s.getLibelle()));
        return dto;
    }

    private Personne chargerPersonne(Long id) {
        return personneRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Personne.class, id));
    }

    private com.gservices.entity.Service chargerService(Long id) {
        return serviceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(com.gservices.entity.Service.class, id));
    }

    private static Specification<Personne> aProfil(String libelleProfil) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            Join<Object, Object> roles = root.join("roles", JoinType.INNER);
            Join<Object, Object> profil = roles.join("profil", JoinType.INNER);
            return cb.and(cb.isTrue(roles.get("etat")),
                    cb.equal(cb.upper(profil.get("libelle")), libelleProfil.toUpperCase()));
        };
    }

    private static Specification<Personne> texte(String filtre) {
        if (!StringUtils.hasText(filtre)) {
            return (root, query, cb) -> cb.conjunction();
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nom")), motif),
                cb.like(cb.lower(root.get("prenom")), motif),
                cb.like(cb.lower(root.get("login")), motif),
                cb.like(cb.lower(root.get("email1")), motif));
    }
}
