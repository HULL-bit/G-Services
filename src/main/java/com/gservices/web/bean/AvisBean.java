package com.gservices.web.bean;

import com.gservices.dto.AvisDto;
import com.gservices.service.AvisService;
import com.gservices.service.AvisService.EtatModeration;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Écran de modération des avis (back-office). */
@Component("avisBean")
@Scope("view")
public class AvisBean extends AbstractAdminBean {

    private final transient AvisService service;

    private String filtre;
    private EtatModeration etat = EtatModeration.EN_ATTENTE;
    private LazyDataModel<AvisDto> model;

    private AvisDto selection = new AvisDto();
    private String reponse;

    public AvisBean(AvisService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        model = new LazyModelService<>(
                (f, pageable) -> service.rechercher(f, etat, pageable),
                AvisDto::getId, () -> filtre);
    }

    public EtatModeration[] getEtats() {
        return EtatModeration.values();
    }

    public void approuver(AvisDto a) {
        if (executer(() -> service.approuver(a.getId()))) {
            info(msg("avis.approuve"));
        }
    }

    public void rejeter(AvisDto a) {
        if (executer(() -> service.rejeter(a.getId()))) {
            info(msg("avis.rejete"));
        }
    }

    public void basculerEtat(AvisDto a) {
        executer(() -> service.basculerEtat(a.getId()));
    }

    public void preparerReponse(AvisDto a) {
        this.selection = a;
        this.reponse = a.getReponsePrestataire();
    }

    public void enregistrerReponse() {
        boolean ok = executer(() -> service.repondre(selection.getId(), reponse));
        if (ok) {
            info(msg("avis.reponse.enregistree"));
            callback("ok", true);
        }
    }

    public long getNombreEnAttente() {
        try {
            return service.nombreEnAttente();
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public String getFiltre() { return filtre; }
    public void setFiltre(String v) { this.filtre = v; }
    public EtatModeration getEtat() { return etat; }
    public void setEtat(EtatModeration v) { this.etat = v; }
    public LazyDataModel<AvisDto> getModel() { return model; }
    public AvisDto getSelection() { return selection; }
    public void setSelection(AvisDto v) { this.selection = v; }
    public String getReponse() { return reponse; }
    public void setReponse(String v) { this.reponse = v; }
}
