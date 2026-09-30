package com.gservices.web.bean;

import com.gservices.dto.InscriptionDto;
import com.gservices.exception.BusinessException;
import com.gservices.service.PersonneService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/** Auto-inscription publique d'un visiteur (profil CLIENT). */
@Component("inscriptionBean")
@Scope("view")
public class InscriptionBean implements Serializable {

    private final transient PersonneService personneService;
    private final transient com.gservices.service.VitrineService vitrineService;
    private InscriptionDto form = new InscriptionDto();
    private boolean inscrit;

    public InscriptionBean(PersonneService personneService,
                           com.gservices.service.VitrineService vitrineService) {
        this.personneService = personneService;
        this.vitrineService = vitrineService;
    }

    /** Catégories proposées au visiteur qui s'inscrit comme fournisseur. */
    public java.util.List<com.gservices.dto.CategorieServiceDto> getCategories() {
        try {
            return vitrineService.categories();
        } catch (RuntimeException e) {
            return java.util.List.of();
        }
    }

    /** Étape « choix du type » → prépare le formulaire client. */
    public void choisirClient() {
        form.setTypeCompte("CLIENT");
    }

    /** Étape « choix du type » → prépare le formulaire fournisseur. */
    public void choisirFournisseur() {
        form.setTypeCompte("FOURNISSEUR");
    }

    /** Repart d'un formulaire vierge (bouton « retour » du tunnel). */
    public void recommencer() {
        form = new InscriptionDto();
        inscrit = false;
    }

    public String valider() {
        try {
            personneService.inscrire(form);
            inscrit = true;
            message(FacesMessage.SEVERITY_INFO, msg("inscription.ok"));
        } catch (BusinessException e) {
            message(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (RuntimeException e) {
            message(FacesMessage.SEVERITY_ERROR, msg("erreur.technique"));
        }
        org.primefaces.PrimeFaces pf = org.primefaces.PrimeFaces.current();
        if (pf != null && pf.isAjaxRequest()) {
            pf.ajax().addCallbackParam("inscrit", inscrit);
        }
        return null;
    }

    private static void message(FacesMessage.Severity sev, String resume) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, resume, null));
    }

    private static String msg(String cle) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getApplication().evaluateExpressionGet(ctx, "#{msg['" + cle + "']}", String.class);
    }

    public InscriptionDto getForm() { return form; }
    public boolean isInscrit() { return inscrit; }
}
