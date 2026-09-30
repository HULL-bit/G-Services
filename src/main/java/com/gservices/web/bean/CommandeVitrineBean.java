package com.gservices.web.bean;

import com.gservices.dto.ArticleDto;
import com.gservices.dto.CommandeDto;
import com.gservices.dto.LigneCommandeDto;
import com.gservices.exception.BusinessException;
import com.gservices.security.SecurityUtils;
import com.gservices.service.CommandeService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tunnel de commande à distance sur la fiche publique d'un service
 * ({@code /public/service.xhtml}). Portée vue : un panier par visite de fiche.
 */
@Component("commandeVitrineBean")
@Scope("view")
public class CommandeVitrineBean implements Serializable {

    private final transient CommandeService commandeService;

    private Long serviceId;
    private List<ArticleDto> articles = new ArrayList<>();
    /** Quantité saisie par article — valeurs stockées telles que fournies par l'IHM (String). */
    private final Map<Long, Object> quantites = new LinkedHashMap<>();

    private String adresseLivraison;
    private String telephoneContact;
    private String commentaire;

    private boolean commandePassee;
    private String referenceCommande;

    public CommandeVitrineBean(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    /** Chargé par {@code <f:viewAction>} sur la fiche service. */
    public void charger() {
        commandePassee = false;
        referenceCommande = null;
        quantites.clear();
        articles = serviceId == null ? List.of() : commandeService.articlesCommandables(serviceId);
    }

    public void commander() {
        try {
            CommandeDto dto = new CommandeDto();
            dto.setServiceId(serviceId);
            dto.setAdresseLivraison(adresseLivraison);
            dto.setTelephoneContact(telephoneContact);
            dto.setCommentaire(commentaire);
            List<LigneCommandeDto> lignes = new ArrayList<>();
            for (ArticleDto a : articles) {
                int q = qte(a.getId());
                if (q > 0) {
                    LigneCommandeDto l = new LigneCommandeDto();
                    l.setArticleId(a.getId());
                    l.setQuantite(q);
                    lignes.add(l);
                }
            }
            dto.setLignes(lignes);
            CommandeDto creee = commandeService.passerCommande(dto);
            commandePassee = true;
            referenceCommande = creee.getReference();
            message(FacesMessage.SEVERITY_INFO, msg("commande.confirmee") + " " + referenceCommande);
        } catch (BusinessException e) {
            message(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (RuntimeException e) {
            message(FacesMessage.SEVERITY_ERROR, msg("erreur.technique"));
        }
    }

    public boolean isConnecte() {
        return SecurityUtils.isAuthenticated();
    }

    public boolean isPanierVide() {
        return articles.stream().noneMatch(a -> qte(a.getId()) > 0);
    }

    public BigDecimal getTotalEstime() {
        BigDecimal total = BigDecimal.ZERO;
        for (ArticleDto a : articles) {
            int q = qte(a.getId());
            if (q > 0 && a.getPrixNet() != null) {
                total = total.add(a.getPrixNet().multiply(BigDecimal.valueOf(q)));
            }
        }
        return total;
    }

    /** Quantité saisie pour un article, tolérante au type fourni par l'IHM. */
    private int qte(Long articleId) {
        Object v = quantites.get(articleId);
        if (v == null) {
            return 0;
        }
        try {
            return Math.max(0, (int) Math.round(Double.parseDouble(v.toString().trim().replace(',', '.'))));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void message(FacesMessage.Severity sev, String resume) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, resume, null));
    }

    private static String msg(String cle) {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            return ctx.getApplication().getResourceBundle(ctx, "msg").getString(cle);
        } catch (RuntimeException e) {
            return cle;
        }
    }

    // --- accesseurs ---
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long v) { this.serviceId = v; }
    public List<ArticleDto> getArticles() { return articles; }
    public Map<Long, Object> getQuantites() { return quantites; }
    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String v) { this.adresseLivraison = v; }
    public String getTelephoneContact() { return telephoneContact; }
    public void setTelephoneContact(String v) { this.telephoneContact = v; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String v) { this.commentaire = v; }
    public boolean isCommandePassee() { return commandePassee; }
    public String getReferenceCommande() { return referenceCommande; }
}
