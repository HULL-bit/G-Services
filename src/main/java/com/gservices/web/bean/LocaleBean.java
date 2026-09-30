package com.gservices.web.bean;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * Gestion de la langue de l'IHM (FR par défaut, EN disponible).
 * Portée session : le choix de l'utilisateur persiste pendant sa visite.
 */
@Component("localeBean")
@SessionScope
public class LocaleBean implements Serializable {

    private static final List<String> SUPPORTEES = List.of("fr", "en");

    private Locale locale = Locale.FRENCH;

    public Locale getLocale()   { return locale; }
    public String getLanguage() { return locale.getLanguage(); }

    public List<String> getLanguesSupportees() { return SUPPORTEES; }

    /** Nom sans préfixe {@code is} : appelé avec argument dans l'EL ({@code #{localeBean.active('fr')}}). */
    public boolean active(String langue) {
        return locale.getLanguage().equals(langue);
    }

    /**
     * Change la langue puis redirige (GET) vers la vue courante : tous les
     * libellés #{msg[...]} sont recalculés proprement, sans re-POST du formulaire.
     */
    public void changeLanguage(String langue) {
        if (langue == null || !SUPPORTEES.contains(langue)) {
            return;
        }
        this.locale = Locale.of(langue);
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            return;
        }
        if (ctx.getViewRoot() != null) {
            ctx.getViewRoot().setLocale(this.locale);
            try {
                ExternalContext ec = ctx.getExternalContext();
                ec.redirect(ec.getRequestContextPath() + ctx.getViewRoot().getViewId());
            } catch (IOException e) {
                // en dernier recours : on reste sur la vue, le nouveau locale
                // s'appliquera au prochain rendu.
                ctx.renderResponse();
            }
        }
    }
}
