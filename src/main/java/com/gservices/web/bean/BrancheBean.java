package com.gservices.web.bean;

import com.gservices.dto.BranchePermissionDto;
import com.gservices.service.BranchePermissionService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("brancheBean")
@Scope("view")
public class BrancheBean extends AbstractAdminBean {

    private final transient BranchePermissionService service;

    private String filtre;
    private LazyDataModel<BranchePermissionDto> model;
    private BranchePermissionDto selection = new BranchePermissionDto();

    public BrancheBean(BranchePermissionService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        this.model = new LazyModelService<>(service::rechercher, BranchePermissionDto::getId, () -> filtre);
    }

    public void preparerCreation() {
        this.selection = new BranchePermissionDto();
    }

    public void preparerEdition(BranchePermissionDto dto) {
        executer(() -> this.selection = service.parId(dto.getId()));
    }

    public void enregistrer() {
        boolean creation = selection.getId() == null;
        boolean ok = executer(() -> {
            if (creation) {
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

    public void basculerEtat(BranchePermissionDto dto) {
        if (executer(() -> service.basculerEtat(dto.getId()))) {
            info(msg("common.etatModifie"));
        }
    }

    public String getFiltre() { return filtre; }
    public void setFiltre(String filtre) { this.filtre = filtre; }
    public LazyDataModel<BranchePermissionDto> getModel() { return model; }
    public BranchePermissionDto getSelection() { return selection; }
    public void setSelection(BranchePermissionDto selection) { this.selection = selection; }
}
