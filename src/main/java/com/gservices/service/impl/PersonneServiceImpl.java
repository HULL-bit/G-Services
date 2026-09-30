package com.gservices.service.impl;

import com.gservices.dto.PermissionEffectiveDto;
import com.gservices.dto.PermissionEffectiveDto.Source;
import com.gservices.dto.PersonneDto;
import com.gservices.entity.Permission;
import com.gservices.entity.Personne;
import com.gservices.entity.PersonnePermission;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.PersonneMapper;
import com.gservices.repository.PermissionRepository;
import com.gservices.repository.PersonneRepository;
import com.gservices.security.SecurityUtils;
import com.gservices.service.PersonneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class PersonneServiceImpl implements PersonneService {

    private static final int LONGUEUR_MDP_MIN = 8;

    private final PersonneRepository repository;
    private final PermissionRepository permissionRepository;
    private final com.gservices.repository.ProfilRepository profilRepository;
    private final com.gservices.repository.CategorieServiceRepository categorieServiceRepository;
    private final com.gservices.repository.ServiceRepository serviceRepository;
    private final PersonneMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public PersonneServiceImpl(PersonneRepository repository,
                               PermissionRepository permissionRepository,
                               com.gservices.repository.ProfilRepository profilRepository,
                               com.gservices.repository.CategorieServiceRepository categorieServiceRepository,
                               com.gservices.repository.ServiceRepository serviceRepository,
                               PersonneMapper mapper,
                               PasswordEncoder passwordEncoder) {
        this.profilRepository = profilRepository;
        this.categorieServiceRepository = categorieServiceRepository;
        this.serviceRepository = serviceRepository;
        this.repository = repository;
        this.permissionRepository = permissionRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_LIRE)")
    public Page<PersonneDto> rechercher(String filtre, Long idProfil, EtatFiltre etat, Pageable pageable) {
        Specification<Personne> spec = filtreSpec(filtre);
        if (idProfil != null) {
            Specification<Personne> profilSpec = (root, query, cb) -> {
                query.distinct(true);
                var roles = root.join("roles", jakarta.persistence.criteria.JoinType.INNER);
                return cb.and(cb.isTrue(roles.get("etat")), cb.equal(roles.get("profil").get("id"), idProfil));
            };
            spec = spec == null ? profilSpec : spec.and(profilSpec);
        }
        if (etat != null && etat != EtatFiltre.TOUS) {
            Specification<Personne> etatSpec = switch (etat) {
                case ACTIFS -> (root, query, cb) -> cb.isTrue(root.get("etat"));
                case INACTIFS -> (root, query, cb) -> cb.isFalse(root.get("etat"));
                case VERROUILLES -> (root, query, cb) -> cb.and(
                        cb.isNotNull(root.get("verrouilleJusqua")),
                        cb.greaterThan(root.get("verrouilleJusqua"), java.time.LocalDateTime.now()));
                default -> null;
            };
            spec = spec == null ? etatSpec : spec.and(etatSpec);
        }
        return repository.findAll(spec, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_LIRE)")
    public List<PersonneDto> toutesActives() {
        return repository.findAll((r, q, cb) -> cb.isTrue(r.get("etat")))
                .stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_LIRE)")
    public List<com.gservices.dto.CategorieServiceDto> categoriesReferentiel() {
        return categorieServiceRepository.findByEtatTrueOrderByOrdreAffichageAscLibelleAsc().stream()
                .map(c -> {
                    com.gservices.dto.CategorieServiceDto d = new com.gservices.dto.CategorieServiceDto();
                    d.setId(c.getId());
                    d.setLibelle(c.getLibelle());
                    d.setIcone(c.getIcone());
                    return d;
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_LIRE)")
    public PersonneDto parId(Long id) {
        return mapper.toDto(charger(id));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_CREER)")
    public PersonneDto creer(PersonneDto dto) {
        if (!StringUtils.hasText(dto.getLogin())) {
            throw new BusinessException("Le login est obligatoire.");
        }
        if (repository.existsByLoginIgnoreCase(dto.getLogin())) {
            throw new BusinessException("Le login « " + dto.getLogin() + " » est déjà pris.");
        }
        validerMotDePasse(dto.getNouveauMotDePasse());

        Personne p = mapper.toEntity(dto);
        p.setLogin(dto.getLogin().trim());
        p.setPassword(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        p.setCategorieSpecialite(resoudreSpecialite(dto.getCategorieSpecialiteId()));
        return mapper.toDto(repository.save(p));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_MODIFIER)")
    public PersonneDto modifier(Long id, PersonneDto dto) {
        Personne p = charger(id);
        mapper.update(p, dto);
        p.setCategorieSpecialite(resoudreSpecialite(dto.getCategorieSpecialiteId()));
        return mapper.toDto(p);
    }

    private com.gservices.entity.CategorieService resoudreSpecialite(Long idCategorie) {
        return idCategorie == null ? null : categorieServiceRepository.findById(idCategorie)
                .orElseThrow(() -> new ResourceNotFoundException(com.gservices.entity.CategorieService.class, idCategorie));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_SUPPRIMER)")
    public void basculerEtat(Long id) {
        Personne p = charger(id);
        SecurityUtils.currentPersonneId()
                .filter(courant -> courant.equals(id) && p.isEtat())
                .ifPresent(courant -> {
                    throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte.");
                });
        p.setEtat(!p.isEtat());
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_MODIFIER)")
    public void reinitialiserMotDePasse(Long id, String nouveauMotDePasse) {
        validerMotDePasse(nouveauMotDePasse);
        Personne p = charger(id);
        p.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        p.setTentativesEchouees(0);
        p.setVerrouilleJusqua(null);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void changerMonMotDePasse(String ancien, String nouveau) {
        Long id = SecurityUtils.currentPersonneId()
                .orElseThrow(() -> new BusinessException("Aucun utilisateur authentifié."));
        Personne p = charger(id);
        if (!passwordEncoder.matches(ancien, p.getPassword())) {
            throw new BusinessException("L'ancien mot de passe est incorrect.");
        }
        validerMotDePasse(nouveau);
        p.setPassword(passwordEncoder.encode(nouveau));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_MODIFIER)")
    public void deverrouiller(Long id) {
        Personne p = charger(id);
        p.setTentativesEchouees(0);
        p.setVerrouilleJusqua(null);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_LIRE)")
    public List<PermissionEffectiveDto> permissionsEffectives(Long idPersonne) {
        Personne p = chargerAvecDroits(idPersonne);
        Set<Long> issuesDuProfil = permissionsIssuesDuProfil(p);
        Map<Long, Boolean> surcharges = surchargesActives(p);

        return permissionRepository.findByEtatTrueOrderByCodeAsc().stream()
                .map(perm -> versDto(perm, issuesDuProfil, surcharges))
                .toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_MODIFIER)")
    public void majPermissions(Long idPersonne, Set<Long> permissionIdsCochees) {
        Personne p = chargerAvecDroits(idPersonne);
        Set<Long> issuesDuProfil = permissionsIssuesDuProfil(p);
        Set<Long> cochees = permissionIdsCochees == null ? Set.of() : permissionIdsCochees;

        Map<Long, PersonnePermission> surchargesExistantes = new HashMap<>();
        p.getPermissionsDirectes().forEach(pp -> surchargesExistantes.put(pp.getPermission().getId(), pp));

        for (Permission perm : permissionRepository.findByEtatTrueOrderByCodeAsc()) {
            boolean coche = cochees.contains(perm.getId());
            boolean duProfil = issuesDuProfil.contains(perm.getId());
            PersonnePermission existante = surchargesExistantes.get(perm.getId());

            if (coche == duProfil) {
                // L'état coché correspond déjà à ce qu'apportent les profils : aucune surcharge nécessaire.
                if (existante != null) {
                    p.getPermissionsDirectes().remove(existante);
                }
            } else if (existante != null) {
                existante.setAccordee(coche);
            } else {
                PersonnePermission pp = new PersonnePermission();
                pp.setPermission(perm);
                pp.setAccordee(coche);
                pp.setEtat(true);
                p.addPermissionDirecte(pp);
            }
        }
    }

    // ------------------------------------------------------------------ util

    private Personne chargerAvecDroits(Long id) {
        return repository.findWithDroitsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Personne.class, id));
    }

    private static Set<Long> permissionsIssuesDuProfil(Personne p) {
        Set<Long> ids = new HashSet<>();
        p.getRoles().stream()
                .filter(r -> r.isEtat() && r.getProfil() != null && r.getProfil().isEtat())
                .flatMap(r -> r.getProfil().getRoleProfils().stream())
                .filter(rp -> rp.isEtat() && rp.getPermission() != null && rp.getPermission().isEtat())
                .forEach(rp -> ids.add(rp.getPermission().getId()));
        return ids;
    }

    private static Map<Long, Boolean> surchargesActives(Personne p) {
        Map<Long, Boolean> map = new HashMap<>();
        p.getPermissionsDirectes().stream()
                .filter(pp -> pp.isEtat() && pp.getPermission() != null)
                .forEach(pp -> map.put(pp.getPermission().getId(), pp.isAccordee()));
        return map;
    }

    private static PermissionEffectiveDto versDto(Permission perm, Set<Long> issuesDuProfil,
                                                  Map<Long, Boolean> surcharges) {
        boolean duProfil = issuesDuProfil.contains(perm.getId());
        Boolean surcharge = surcharges.get(perm.getId());
        Source source = surcharge == null
                ? (duProfil ? Source.PROFIL : Source.AUCUNE)
                : (surcharge ? Source.DIRECTE_ACCORDEE : Source.DIRECTE_RETIREE);
        String brancheLibelle = perm.getBranchePermission() == null ? "" : perm.getBranchePermission().getLibelle();
        return new PermissionEffectiveDto(perm.getId(), perm.getCode(), perm.getLibelle(),
                perm.getActionAutorisee(), brancheLibelle, duProfil, source);
    }

    private Personne charger(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Personne.class, id));
    }

    private void validerMotDePasse(String mdp) {
        if (mdp == null || mdp.length() < LONGUEUR_MDP_MIN) {
            throw new BusinessException(
                    "Le mot de passe doit contenir au moins " + LONGUEUR_MDP_MIN + " caractères.");
        }
    }

    private static Specification<Personne> filtreSpec(String filtre) {
        if (!StringUtils.hasText(filtre)) {
            return null;
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nom")), motif),
                cb.like(cb.lower(root.get("prenom")), motif),
                cb.like(cb.lower(root.get("login")), motif),
                cb.like(cb.lower(root.get("email1")), motif));
    }

    @Override
    public void inscrire(com.gservices.dto.InscriptionDto dto) {
        if (!StringUtils.hasText(dto.getLogin()) || repository.existsByLoginIgnoreCase(dto.getLogin())) {
            throw new BusinessException("Ce nom d'utilisateur est déjà pris.");
        }
        if (StringUtils.hasText(dto.getEmail()) && repository.existsByEmail1IgnoreCase(dto.getEmail())) {
            throw new BusinessException("Un compte existe déjà avec cet e-mail.");
        }
        if (dto.getMotDePasseConfirme() != null && !dto.getMotDePasseConfirme().equals(dto.getMotDePasse())) {
            throw new BusinessException("Les deux mots de passe ne correspondent pas.");
        }
        validerMotDePasse(dto.getMotDePasse());

        boolean fournisseur = dto.isFournisseur();
        com.gservices.entity.CategorieService specialite = null;
        if (fournisseur) {
            if (dto.getCategorieSpecialiteId() == null) {
                throw new BusinessException("Un fournisseur doit choisir sa catégorie de spécialité.");
            }
            specialite = resoudreSpecialite(dto.getCategorieSpecialiteId());
        }

        Personne p = new Personne();
        p.setNom(dto.getNom().trim());
        p.setPrenom(dto.getPrenom().trim());
        p.setLogin(dto.getLogin().trim());
        p.setEmail1(dto.getEmail() == null ? null : dto.getEmail().trim());
        p.setPassword(passwordEncoder.encode(dto.getMotDePasse()));
        p.setEtat(true);
        p.setValide(false);                 // en attente de validation par un administrateur
        p.setCategorieSpecialite(specialite);
        repository.save(p);

        String libelleProfil = fournisseur ? "PRESTATAIRE" : "CLIENT";
        profilRepository.findByLibelleIgnoreCase(libelleProfil).ifPresent(profil -> {
            com.gservices.entity.Role role = new com.gservices.entity.Role();
            role.setProfil(profil);
            role.setDateAttribution(java.time.LocalDateTime.now());
            role.setEtat(true);
            p.addRole(role);
        });

        if (fournisseur && StringUtils.hasText(dto.getServiceLibelle())) {
            com.gservices.entity.Service s = new com.gservices.entity.Service();
            s.setLibelle(dto.getServiceLibelle().trim());
            s.setCategorieService(specialite);
            s.setProprietaire(p);
            s.setValide(false);             // en attente de validation
            s.setEtat(true);
            serviceRepository.save(s);
        }
    }
}
