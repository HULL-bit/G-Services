package com.gservices.web.bean;

import com.gservices.dto.SectionMenuDto;
import com.gservices.service.MenuService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * Menu latéral d'administration — construit dynamiquement à partir des
 * {@code BranchePermission} / {@code Permission} de l'utilisateur courant
 * ({@link MenuService}). Portée vue : recalculé à chaque navigation.
 */
@Component("menuBean")
@org.springframework.web.context.annotation.RequestScope
public class MenuBean implements Serializable {

    private final transient MenuService menuService;
    private List<SectionMenuDto> sections;

    public MenuBean(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostConstruct
    void init() {
        this.sections = menuService.menuCourant();
    }

    public List<SectionMenuDto> getSections() {
        return sections;
    }

    /** {@code true} si l'URL passée correspond à la vue actuellement affichée. */
    public boolean actif(String url) {
        if (url == null) {
            return false;
        }
        FacesContext ctx = FacesContext.getCurrentInstance();
        String viewId = ctx == null || ctx.getViewRoot() == null ? "" : ctx.getViewRoot().getViewId();
        return url.equals(viewId);
    }
}
