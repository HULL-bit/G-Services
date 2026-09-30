package com.gservices.web.bean;

import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.PrimeFaces;
import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;

/**
 * Socle commun aux beans d'administration : messages growl et exécution
 * d'actions avec conversion des exceptions métier / d'accès en message d'erreur.
 */
public abstract class AbstractAdminBean implements Serializable {

    protected void info(String resume) {
        addMessage(FacesMessage.SEVERITY_INFO, resume);
    }

    protected void erreur(String resume) {
        addMessage(FacesMessage.SEVERITY_ERROR, resume);
    }

    protected void addMessage(FacesMessage.Severity severite, String resume) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severite, resume, null));
    }

    /** Ajoute un paramètre de retour AJAX exploitable dans {@code oncomplete}. */
    protected void callback(String nom, Object valeur) {
        PrimeFaces pf = PrimeFaces.current();
        if (pf != null && pf.isAjaxRequest()) {
            pf.ajax().addCallbackParam(nom, valeur);
        }
    }

    /** Résout une clé i18n dans la langue courante. */
    protected String msg(String cle) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getApplication().evaluateExpressionGet(ctx, "#{msg['" + cle + "']}", String.class);
    }

    /**
     * Exécute {@code action} ; en cas d'échec métier ou d'accès refusé, publie un
     * growl d'erreur et renvoie {@code false} (le dialogue reste ouvert).
     */
    protected boolean executer(Runnable action) {
        try {
            action.run();
            return true;
        } catch (AccessDeniedException e) {
            erreur(msg("erreur.acces"));
        } catch (BusinessException | ResourceNotFoundException e) {
            erreur(e.getMessage());
        }
        return false;
    }
}
