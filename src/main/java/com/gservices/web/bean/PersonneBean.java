package com.gservices.web.bean;

import com.gservices.dto.PermissionEffectiveDto;
import com.gservices.dto.PersonneDto;
import com.gservices.dto.ProfilDto;
import com.gservices.dto.RoleDto;
import com.gservices.entity.Sexe;
import com.gservices.service.PersonneService;
import com.gservices.service.ProfilService;
import com.gservices.service.RoleService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("personneBean")
@Scope("view")
public class PersonneBean extends AbstractAdminBean {

    private final transient PersonneService service;
    private final transient ProfilService profilService;
    private final transient RoleService roleService;

    private String filtre;
    private Long filtreProfil;
    private PersonneService.EtatFiltre filtreEtat = PersonneService.EtatFiltre.TOUS;
    private LazyDataModel<PersonneDto> model;
    private PersonneDto selection = new PersonneDto();

    private String nouveauMotDePasse;

    // Gestion des profils (classe d'association Role)
    private PersonneDto personneRoles;
    private List<RoleDto> rolesPersonne;
    private List<ProfilDto> tousProfils;
    private Long profilAAjouter;

    // Permissions effectives (cochées / décochées par utilisateur)
    private PersonneDto personnePerms;
    private List<PermissionEffectiveDto> permsEffectives;
    private Map<Long, Boolean> coche = new LinkedHashMap<>();

    public PersonneBean(PersonneService service, ProfilService profilService, RoleService roleService) {
        this.service = service;
        this.profilService = profilService;
        this.roleService = roleService;
    }

    private List<com.gservices.dto.CategorieServiceDto> categoriesSpecialite = java.util.List.of();

    @PostConstruct
    void init() {
        this.model = new LazyModelService<>(
                (f, pageable) -> service.rechercher(f, filtreProfil, filtreEtat, pageable),
                PersonneDto::getId, () -> filtre);
        try {
            this.categoriesSpecialite = service.categoriesReferentiel();
        } catch (RuntimeException e) {
            this.categoriesSpecialite = java.util.List.of();
        }
        try {
            this.tousProfils = profilService.tousActifs();
        } catch (RuntimeException e) {
            this.tousProfils = java.util.List.of();
        }
    }

    public Sexe[] getSexes() {
        return Sexe.values();
    }

    /** Catégories de services proposées comme spécialité d'un fournisseur. */
    public List<com.gservices.dto.CategorieServiceDto> getCategoriesSpecialite() {
        return categoriesSpecialite;
    }

    public void preparerCreation() {
        this.selection = new PersonneDto();
        this.nouveauMotDePasse = null;
    }

    public void preparerEdition(PersonneDto dto) {
        executer(() -> this.selection = service.parId(dto.getId()));
    }

    public void enregistrer() {
        boolean creation = selection.getId() == null;
        boolean ok = executer(() -> {
            if (creation) {
                selection.setNouveauMotDePasse(nouveauMotDePasse);
                service.creer(selection);
            } else {
                service.modifier(selection.getId(), selection);
            }
        });
        if (ok) {
            info(msg("common.enregistre"));
            callback("ok", true);
        }
    }

    public void basculerEtat(PersonneDto dto) {
        if (executer(() -> service.basculerEtat(dto.getId()))) {
            info(msg("common.etatModifie"));
        }
    }

    public void deverrouiller(PersonneDto dto) {
        if (executer(() -> service.deverrouiller(dto.getId()))) {
            info(msg("personne.deverrouille"));
        }
    }

    public void preparerReinit(PersonneDto dto) {
        this.selection = dto;
        this.nouveauMotDePasse = null;
    }

    public void reinitialiser() {
        boolean ok = executer(() -> service.reinitialiserMotDePasse(selection.getId(), nouveauMotDePasse));
        if (ok) {
            info(msg("personne.mdpReinit"));
            callback("ok", true);
        }
    }

    // ------------------------------------------------------------ profils / Role

    public void gererProfils(PersonneDto dto) {
        boolean ok = executer(() -> {
            this.personneRoles = dto;
            this.rolesPersonne = roleService.parPersonne(dto.getId());
            if (tousProfils == null) {
                this.tousProfils = profilService.tousActifs();
            }
            this.profilAAjouter = null;
        });
        if (ok) {
            callback("profils", true);
        }
    }

    public void ajouterProfil() {
        if (profilAAjouter == null) {
            return;
        }
        boolean ok = executer(() -> {
            roleService.attribuer(personneRoles.getId(), profilAAjouter);
            this.rolesPersonne = roleService.parPersonne(personneRoles.getId());
            this.profilAAjouter = null;
        });
        if (ok) {
            info(msg("personne.profilAjoute"));
        }
    }

    public void voirPermissions(PersonneDto dto) {
        boolean ok = executer(() -> {
            this.personnePerms = dto;
            this.permsEffectives = service.permissionsEffectives(dto.getId());
            this.coche = new LinkedHashMap<>();
            // la case initiale reflète l'état EFFECTIF (profil, éventuellement surchargé)
            this.permsEffectives.forEach(pe -> coche.put(pe.getPermissionId(), pe.isEffective()));
        });
        if (ok) {
            callback("perms", true);
        }
    }

    public void enregistrerPermissions() {
        java.util.Set<Long> cochees = new java.util.LinkedHashSet<>();
        coche.forEach((id, v) -> { if (Boolean.TRUE.equals(v)) { cochees.add(id); } });
        boolean ok = executer(() -> {
            service.majPermissions(personnePerms.getId(), cochees);
            this.permsEffectives = service.permissionsEffectives(personnePerms.getId());
        });
        if (ok) {
            info(msg("personne.permsEnregistrees"));
            callback("permsOk", true);
        }
    }

    public void basculerRole(RoleDto role) {
        boolean ok = executer(() -> {
            roleService.basculerEtat(role.getId());
            this.rolesPersonne = roleService.parPersonne(personneRoles.getId());
        });
        if (ok) {
            info(msg("common.etatModifie"));
        }
    }

    public String getFiltre() { return filtre; }
    public void setFiltre(String filtre) { this.filtre = filtre; }
    public Long getFiltreProfil() { return filtreProfil; }
    public void setFiltreProfil(Long filtreProfil) { this.filtreProfil = filtreProfil; }
    public PersonneService.EtatFiltre getFiltreEtat() { return filtreEtat; }
    public void setFiltreEtat(PersonneService.EtatFiltre filtreEtat) { this.filtreEtat = filtreEtat; }
    public PersonneService.EtatFiltre[] getEtatsFiltre() { return PersonneService.EtatFiltre.values(); }
    public LazyDataModel<PersonneDto> getModel() { return model; }
    public PersonneDto getSelection() { return selection; }
    public void setSelection(PersonneDto selection) { this.selection = selection; }
    public String getNouveauMotDePasse() { return nouveauMotDePasse; }
    public void setNouveauMotDePasse(String v) { this.nouveauMotDePasse = v; }
    public PersonneDto getPersonneRoles() { return personneRoles; }
    public List<RoleDto> getRolesPersonne() { return rolesPersonne; }
    public List<ProfilDto> getTousProfils() { return tousProfils; }
    public Long getProfilAAjouter() { return profilAAjouter; }
    public void setProfilAAjouter(Long v) { this.profilAAjouter = v; }
    public PersonneDto getPersonnePerms() { return personnePerms; }
    public List<PermissionEffectiveDto> getPermsEffectives() { return permsEffectives; }
    public Map<Long, Boolean> getCoche() { return coche; }
    public void setCoche(Map<Long, Boolean> coche) { this.coche = coche; }
}
