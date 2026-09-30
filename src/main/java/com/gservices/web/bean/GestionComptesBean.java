package com.gservices.web.bean;

import com.gservices.dto.CategorieServiceDto;
import com.gservices.dto.CreationCompteDto;
import com.gservices.dto.PersonneDto;
import com.gservices.dto.ServiceDto;
import com.gservices.service.GestionComptesService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Écrans back-office « Fournisseurs », « Clients » et « Validations »
 * (branche 8000). Un seul bean : les trois pages partagent le référentiel des
 * catégories et la file d'attente.
 */
@Component("comptesBean")
@Scope("view")
public class GestionComptesBean extends AbstractAdminBean {

    private final transient GestionComptesService service;

    private String filtreFournisseur;
    private String filtreClient;
    private LazyDataModel<PersonneDto> fournisseurs;
    private LazyDataModel<PersonneDto> clients;

    private CreationCompteDto nouveauFournisseur = new CreationCompteDto();
    private CreationCompteDto nouveauClient = new CreationCompteDto();

    private List<CategorieServiceDto> categories = List.of();
    private List<PersonneDto> comptesEnAttente = List.of();
    private List<ServiceDto> servicesEnAttente = List.of();

    public GestionComptesBean(GestionComptesService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        fournisseurs = new LazyModelService<>(
                (f, p) -> service.rechercherFournisseurs(f, p), PersonneDto::getId, () -> filtreFournisseur);
        clients = new LazyModelService<>(
                (f, p) -> service.rechercherClients(f, p), PersonneDto::getId, () -> filtreClient);
        try {
            categories = service.categoriesReferentiel();
        } catch (RuntimeException e) {
            categories = List.of();
        }
        rechargerFile();
    }

    private void rechargerFile() {
        try {
            comptesEnAttente = service.comptesEnAttente();
            servicesEnAttente = service.servicesEnAttente();
        } catch (RuntimeException e) {
            comptesEnAttente = List.of();
            servicesEnAttente = List.of();
        }
    }

    // -------------------------------------------------- création
    public void creerFournisseur() {
        if (executer(() -> service.creerFournisseur(nouveauFournisseur))) {
            info(msg("comptes.fournisseur.cree"));
            nouveauFournisseur = new CreationCompteDto();
            callback("ok", true);
        }
    }

    public void creerClient() {
        if (executer(() -> service.creerClient(nouveauClient))) {
            info(msg("comptes.client.cree"));
            nouveauClient = new CreationCompteDto();
            callback("ok", true);
        }
    }

    // -------------------------------------------------- activation
    public void basculerFournisseur(PersonneDto p) {
        if (executer(() -> service.basculerFournisseur(p.getId()))) {
            info(msg("common.majEffectuee"));
        }
    }

    public void basculerClient(PersonneDto p) {
        if (executer(() -> service.basculerClient(p.getId()))) {
            info(msg("common.majEffectuee"));
        }
    }

    // -------------------------------------------------- validation
    public void validerCompte(PersonneDto p) {
        if (executer(() -> service.validerCompte(p.getId()))) {
            info(msg("comptes.valide.compte"));
            rechargerFile();
        }
    }

    public void refuserCompte(PersonneDto p) {
        if (executer(() -> service.refuserCompte(p.getId()))) {
            info(msg("comptes.refuse.compte"));
            rechargerFile();
        }
    }

    public void validerService(ServiceDto s) {
        if (executer(() -> service.validerService(s.getId()))) {
            info(msg("comptes.valide.service"));
            rechargerFile();
        }
    }

    public void refuserService(ServiceDto s) {
        if (executer(() -> service.refuserService(s.getId()))) {
            info(msg("comptes.refuse.service"));
            rechargerFile();
        }
    }

    public int getNbEnAttente() {
        return comptesEnAttente.size() + servicesEnAttente.size();
    }

    // -------------------------------------------------- accesseurs
    public String getFiltreFournisseur() { return filtreFournisseur; }
    public void setFiltreFournisseur(String v) { this.filtreFournisseur = v; }
    public String getFiltreClient() { return filtreClient; }
    public void setFiltreClient(String v) { this.filtreClient = v; }
    public LazyDataModel<PersonneDto> getFournisseurs() { return fournisseurs; }
    public LazyDataModel<PersonneDto> getClients() { return clients; }
    public CreationCompteDto getNouveauFournisseur() { return nouveauFournisseur; }
    public CreationCompteDto getNouveauClient() { return nouveauClient; }
    public List<CategorieServiceDto> getCategories() { return categories; }
    public List<PersonneDto> getComptesEnAttente() { return comptesEnAttente; }
    public List<ServiceDto> getServicesEnAttente() { return servicesEnAttente; }
}
