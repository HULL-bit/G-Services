package com.gservices.web.bean;

import com.gservices.security.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.Serializable;

/**
 * Expose l'utilisateur courant à l'IHM JSF :
 * <pre>
 *   #{utilisateurCourant.authentifie}
 *   #{utilisateurCourant.nomComplet}
 *   #{utilisateurCourant.autorise('PERSONNE_CREER')}   &lt;- masquage des actions
 * </pre>
 */
@Component("utilisateurCourant")
@RequestScope
public class UtilisateurCourantBean implements Serializable {

    public boolean isAuthentifie() {
        return SecurityUtils.isAuthenticated();
    }

    public String getLogin() {
        return SecurityUtils.currentLogin().orElse(null);
    }

    public String getNomComplet() {
        return SecurityUtils.currentUser()
                .map(u -> u.getNomComplet())
                .orElse(null);
    }

    public Long getPersonneId() {
        return SecurityUtils.currentPersonneId().orElse(null);
    }

    /** {@code true} si l'utilisateur détient la permission de code donné. */
    public boolean autorise(String codePermission) {
        return SecurityUtils.hasPermission(codePermission);
    }
}
