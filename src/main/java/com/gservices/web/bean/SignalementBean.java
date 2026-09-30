package com.gservices.web.bean;

import com.gservices.dto.SanctionDto;
import com.gservices.dto.SignalementDto;
import com.gservices.entity.StatutSignalement;
import com.gservices.service.SignalementService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Écran « Signalements & sanctions » (back-office) — surveillance de la plateforme. */
@Component("signalementBean")
@Scope("view")
public class SignalementBean extends AbstractAdminBean {

    private final transient SignalementService service;

    private String filtre;
    private StatutSignalement statut = StatutSignalement.NOUVEAU;
    private LazyDataModel<SignalementDto> model;

    private String filtreBloques;
    private LazyDataModel<SanctionDto> modelBloques;

    private SignalementDto selection = new SignalementDto();
    private String motifSanction;

    public SignalementBean(SignalementService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        model = new LazyModelService<>(
                (f, pageable) -> service.rechercher(f, statut, pageable),
                SignalementDto::getId, () -> filtre);
        modelBloques = new LazyModelService<>(
                (f, pageable) -> service.servicesBloques(f, pageable),
                SanctionDto::getId, () -> filtreBloques);
    }

    public StatutSignalement[] getStatuts() {
        return StatutSignalement.values();
    }

    public long getNombreNouveaux() {
        return service.nombreNouveaux();
    }

    public long getNombreBloques() {
        return service.nombreServicesBloques();
    }

    public void rejeter(SignalementDto s) {
        if (executer(() -> service.rejeter(s.getId()))) {
            info(msg("signalement.rejete"));
        }
    }

    public void preparerSanction(SignalementDto s) {
        selection = s;
        motifSanction = null;
    }

    public void confirmerSanction() {
        if (executer(() -> service.sanctionner(selection.getId(), motifSanction))) {
            info(msg("signalement.sanctionne"));
            callback("ok", true);
        }
    }

    public void lever(SanctionDto s) {
        if (executer(() -> service.lever(s.getId()))) {
            info(msg("sanction.levee"));
        }
    }

    // ------------------------------------------------------------ accès JSF

    public String getFiltre() { return filtre; }
    public void setFiltre(String v) { this.filtre = v; }
    public StatutSignalement getStatut() { return statut; }
    public void setStatut(StatutSignalement v) { this.statut = v; }
    public LazyDataModel<SignalementDto> getModel() { return model; }

    public String getFiltreBloques() { return filtreBloques; }
    public void setFiltreBloques(String v) { this.filtreBloques = v; }
    public LazyDataModel<SanctionDto> getModelBloques() { return modelBloques; }

    public SignalementDto getSelection() { return selection; }
    public String getMotifSanction() { return motifSanction; }
    public void setMotifSanction(String v) { this.motifSanction = v; }
}
