package com.gservices.web.bean;

import com.gservices.dto.BranchePermissionDto;
import com.gservices.dto.PermissionDto;
import com.gservices.dto.ProfilDto;
import com.gservices.service.BranchePermissionService;
import com.gservices.service.PermissionService;
import com.gservices.service.ProfilService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component("profilBean")
@Scope("view")
public class ProfilBean extends AbstractAdminBean {

    private final transient ProfilService service;
    private final transient PermissionService permissionService;
    private final transient BranchePermissionService brancheService;

    private String filtre;
    private LazyDataModel<ProfilDto> model;
    private ProfilDto selection = new ProfilDto();

    // Édition des droits
    private ProfilDto profilDroits;
    private Set<Long> permissionsSelectionnees = new LinkedHashSet<>();
    private Set<Long> branchesSelectionnees = new LinkedHashSet<>();
    private List<PermissionDto> toutesPermissions;
    private List<BranchePermissionDto> toutesBranches;

    public ProfilBean(ProfilService service, PermissionService permissionService,
                      BranchePermissionService brancheService) {
        this.service = service;
        this.permissionService = permissionService;
        this.brancheService = brancheService;
    }

    @PostConstruct
    void init() {
        this.model = new LazyModelService<>(service::rechercher, ProfilDto::getId, () -> filtre);
    }

    public void preparerCreation() {
        this.selection = new ProfilDto();
    }

    public void preparerEdition(ProfilDto dto) {
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

    public void basculerEtat(ProfilDto dto) {
        if (executer(() -> service.basculerEtat(dto.getId()))) {
            info(msg("common.etatModifie"));
        }
    }

    // ------------------------------------------------------------ droits

    public void gererDroits(ProfilDto dto) {
        boolean ok = executer(() -> {
            this.profilDroits = service.parId(dto.getId());
            this.permissionsSelectionnees = new LinkedHashSet<>(profilDroits.getPermissionIds());
            this.branchesSelectionnees = new LinkedHashSet<>(profilDroits.getBrancheIds());
            if (toutesPermissions == null) {
                this.toutesPermissions = permissionService.toutesActives();
            }
            if (toutesBranches == null) {
                this.toutesBranches = brancheService.toutesActives();
            }
        });
        if (ok) {
            callback("droits", true);
        }
    }

    public void enregistrerDroits() {
        boolean ok = executer(() -> {
            service.affecterPermissions(profilDroits.getId(), permissionsSelectionnees);
            service.affecterBranches(profilDroits.getId(), branchesSelectionnees);
        });
        if (ok) {
            info(msg("profil.droitsEnregistres"));
            callback("ok", true);
        }
    }

    public String getFiltre() { return filtre; }
    public void setFiltre(String filtre) { this.filtre = filtre; }
    public LazyDataModel<ProfilDto> getModel() { return model; }
    public ProfilDto getSelection() { return selection; }
    public void setSelection(ProfilDto selection) { this.selection = selection; }
    public ProfilDto getProfilDroits() { return profilDroits; }
    public Set<Long> getPermissionsSelectionnees() { return permissionsSelectionnees; }
    public void setPermissionsSelectionnees(Set<Long> v) { this.permissionsSelectionnees = v; }
    public Set<Long> getBranchesSelectionnees() { return branchesSelectionnees; }
    public void setBranchesSelectionnees(Set<Long> v) { this.branchesSelectionnees = v; }
    public List<PermissionDto> getToutesPermissions() { return toutesPermissions; }
    public List<BranchePermissionDto> getToutesBranches() { return toutesBranches; }
}
