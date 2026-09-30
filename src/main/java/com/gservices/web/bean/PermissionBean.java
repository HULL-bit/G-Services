package com.gservices.web.bean;

import com.gservices.dto.BranchePermissionDto;
import com.gservices.dto.PermissionDto;
import com.gservices.service.BranchePermissionService;
import com.gservices.service.PermissionService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("permissionBean")
@Scope("view")
public class PermissionBean extends AbstractAdminBean {

    private final transient PermissionService service;
    private final transient BranchePermissionService brancheService;

    private String filtre;
    private LazyDataModel<PermissionDto> model;
    private PermissionDto selection = new PermissionDto();
    private List<BranchePermissionDto> branches;

    public PermissionBean(PermissionService service, BranchePermissionService brancheService) {
        this.service = service;
        this.brancheService = brancheService;
    }

    @PostConstruct
    void init() {
        this.model = new LazyModelService<>(service::rechercher, PermissionDto::getId, () -> filtre);
        this.branches = brancheService.toutesActives();
    }

    public void preparerCreation() {
        this.selection = new PermissionDto();
    }

    public void preparerEdition(PermissionDto dto) {
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

    public void basculerEtat(PermissionDto dto) {
        if (executer(() -> service.basculerEtat(dto.getId()))) {
            info(msg("common.etatModifie"));
        }
    }

    public String getFiltre() { return filtre; }
    public void setFiltre(String filtre) { this.filtre = filtre; }
    public LazyDataModel<PermissionDto> getModel() { return model; }
    public PermissionDto getSelection() { return selection; }
    public void setSelection(PermissionDto selection) { this.selection = selection; }
    public List<BranchePermissionDto> getBranches() { return branches; }
}
