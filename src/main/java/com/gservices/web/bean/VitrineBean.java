package com.gservices.web.bean;

import com.gservices.dto.*;
import com.gservices.exception.BusinessException;
import com.gservices.security.SecurityUtils;
import com.gservices.service.AvisService;
import com.gservices.service.FavoriService;
import com.gservices.service.SignalementService;
import com.gservices.service.VitrineService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean des pages <strong>publiques</strong> : catalogue (catégories → services) et
 * fiche d'un service (prestataire, horaires, offre, avis + dépôt d'avis).
 */
@Component("vitrineBean")
@Scope("view")
public class VitrineBean {

    private final transient VitrineService vitrine;
    private final transient AvisService avisService;
    private final transient FavoriService favoriService;
    private final transient SignalementService signalementService;

    // --- catalogue ---
    private List<CategorieServiceDto> categories = new ArrayList<>();
    private Long categorieActive;
    private List<ServiceVitrineDto> services = new ArrayList<>();
    private final Map<Long, List<ServiceVitrineDto>> parCategorie = new LinkedHashMap<>();

    // --- fiche service ---
    private Long serviceId;
    private ServiceDetailDto detail;

    // --- dépôt d'avis ---
    private int maNote = 5;
    private String monCommentaire;
    private AvisDto monAvis;

    // --- réponse du prestataire (propriétaire du service) ---
    private boolean proprietaire;
    private final Map<Long, String> reponses = new LinkedHashMap<>();

    // --- favori ---
    private boolean favori;

    // --- signalement ---
    private com.gservices.entity.MotifSignalement motifSignalement;
    private String descriptionSignalement;
    private boolean signalementEnvoye;

    public VitrineBean(VitrineService vitrine, AvisService avisService, FavoriService favoriService,
                       SignalementService signalementService) {
        this.vitrine = vitrine;
        this.avisService = avisService;
        this.favoriService = favoriService;
        this.signalementService = signalementService;
    }

    @PostConstruct
    void init() {
        try {
            categories = vitrine.categories();
            for (CategorieServiceDto c : categories) {
                parCategorie.put(c.getId(), vitrine.servicesParCategorie(c.getId()));
            }
            if (!categories.isEmpty()) {
                selectionnerCategorie(categories.get(0).getId());
            }
        } catch (RuntimeException e) {
            categories = new ArrayList<>();
        }
    }

    // ---------------------------------------------------- catalogue

    public void selectionnerCategorie(Long id) {
        this.categorieActive = id;
        this.services = parCategorie.getOrDefault(id, new ArrayList<>());
    }

    public List<ServiceVitrineDto> servicesDe(Long idCategorie) {
        return parCategorie.getOrDefault(idCategorie, List.of());
    }

    public Map<Long, List<ServiceVitrineDto>> getParCategorie() {
        return parCategorie;
    }

    /** Points de la catégorie affichée, pour la carte du catalogue. */
    public String getServicesJson() {
        return toJson(services);
    }

    /** Le point unique de la fiche prestataire courante, pour sa carte. */
    public String getPrestataireJson() {
        if (detail == null || detail.getPrestataire() == null || detail.getPrestataire().getLatitude() == null) {
            return "null";
        }
        InformationServiceDto info = detail.getPrestataire();
        return "{\"lat\":" + info.getLatitude() + ",\"lng\":" + info.getLongitude()
                + ",\"libelle\":\"" + jsEsc(detail.getService().getLibelle()) + "\""
                + ",\"icone\":\"" + jsEsc(detail.getService().getCategorieIcone()) + "\""
                + ",\"pays\":\"" + jsEsc(info.getPositionLibelle()) + "\"}";
    }

    private String toJson(List<ServiceVitrineDto> liste) {
        StringBuilder sb = new StringBuilder("[");
        String ctx = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        boolean first = true;
        for (ServiceVitrineDto s : liste) {
            if (s.getLatitude() == null || s.getLongitude() == null) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append("{\"id\":").append(s.getId())
              .append(",\"libelle\":\"").append(jsEsc(s.getLibelle())).append('"')
              .append(",\"icone\":\"").append(jsEsc(s.getCategorieIcone())).append('"')
              .append(",\"pays\":\"").append(jsEsc(s.getVilleLibelle())).append('"')
              .append(",\"lat\":").append(s.getLatitude())
              .append(",\"lng\":").append(s.getLongitude())
              .append(",\"url\":\"").append(ctx).append("/public/service.xhtml?id=").append(s.getId()).append("\"}");
        }
        return sb.append(']').toString();
    }

    private static String jsEsc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("<", "\\u003c").replace("\n", " ");
    }

    // ---------------------------------------------------- fiche service

    /** Appelée par {@code f:viewAction} sur {@code public/service.xhtml}. */
    public void chargerService() {
        if (serviceId == null) {
            return;
        }
        try {
            detail = vitrine.serviceDetail(serviceId);
            monAvis = avisService.monAvisService(serviceId);
            proprietaire = avisService.estProprietaireDuService(serviceId);
            favori = favoriService.estFavori(serviceId);
            reponses.clear();
            if (proprietaire && detail != null) {
                detail.getAvis().forEach(a -> reponses.put(a.getId(), a.getReponsePrestataire()));
            }
        } catch (RuntimeException e) {
            detail = null;
        }
    }

    public boolean isConnecte() {
        return SecurityUtils.isAuthenticated();
    }

    public boolean isPeutDeposer() {
        return isConnecte() && monAvis == null && detail != null && !proprietaire;
    }

    public boolean isProprietaire() {
        return proprietaire;
    }

    public boolean isFavori() {
        return favori;
    }

    /** Ajoute/retire le service courant des favoris du client connecté. */
    public void basculerFavori() {
        if (serviceId == null) {
            return;
        }
        try {
            favori = favoriService.basculerFavori(serviceId);
        } catch (BusinessException e) {
            message(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    /** Dépose un signalement sur le service courant (§3 « surveillance de la plateforme »). */
    public void signaler() {
        if (serviceId == null) {
            return;
        }
        try {
            signalementService.signaler(serviceId, motifSignalement, descriptionSignalement);
            signalementEnvoye = true;
            motifSignalement = null;
            descriptionSignalement = null;
            message(FacesMessage.SEVERITY_INFO, msg("vitrine.signalement.ok"));
        } catch (BusinessException e) {
            message(FacesMessage.SEVERITY_WARN, e.getMessage());
        }
    }

    public com.gservices.entity.MotifSignalement[] getMotifsSignalement() {
        return com.gservices.entity.MotifSignalement.values();
    }

    public com.gservices.entity.MotifSignalement getMotifSignalement() {
        return motifSignalement;
    }

    public void setMotifSignalement(com.gservices.entity.MotifSignalement v) {
        this.motifSignalement = v;
    }

    public String getDescriptionSignalement() {
        return descriptionSignalement;
    }

    public void setDescriptionSignalement(String v) {
        this.descriptionSignalement = v;
    }

    public boolean isSignalementEnvoye() {
        return signalementEnvoye;
    }

    public Map<Long, String> getReponses() {
        return reponses;
    }

    public void deposerAvis() {
        try {
            avisService.deposerAvisService(serviceId, maNote, monCommentaire);
            monCommentaire = null;
            chargerService();
            message(FacesMessage.SEVERITY_INFO, msg("vitrine.avis.merci"));
        } catch (BusinessException e) {
            message(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (RuntimeException e) {
            message(FacesMessage.SEVERITY_ERROR, msg("erreur.technique"));
        }
    }

    public void repondre(Long idAvis) {
        try {
            avisService.repondreProprietaire(idAvis, reponses.get(idAvis));
            chargerService();
            message(FacesMessage.SEVERITY_INFO, msg("vitrine.avis.reponseOk"));
        } catch (BusinessException e) {
            message(FacesMessage.SEVERITY_WARN, e.getMessage());
        } catch (RuntimeException e) {
            message(FacesMessage.SEVERITY_ERROR, msg("erreur.technique"));
        }
    }

    // ---------------------------------------------------- util

    private static void message(FacesMessage.Severity sev, String resume) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, resume, null));
    }

    private static String msg(String cle) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getApplication().evaluateExpressionGet(ctx, "#{msg['" + cle + "']}", String.class);
    }

    /** [5,4,3,2,1] — pour l'histogramme des notes (EL n'a pas de littéral de liste fiable ici). */
    public List<Integer> getNotesDesc() {
        return List.of(5, 4, 3, 2, 1);
    }

    public long repartition(int note) {
        if (detail == null || detail.getSynthese() == null || note < 1 || note > 5) {
            return 0;
        }
        return detail.getSynthese().getRepartition()[note - 1];
    }

    public int pourcentage(int note) {
        if (detail == null || detail.getSynthese() == null || detail.getSynthese().getTotal() == 0) {
            return 0;
        }
        return (int) (repartition(note) * 100 / detail.getSynthese().getTotal());
    }

    public String etoiles(double note) {
        int pleines = (int) Math.round(note);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= pleines ? '★' : '☆');
        }
        return sb.toString();
    }

    // ---------------------------------------------------- accessors

    public List<CategorieServiceDto> getCategories() { return categories; }
    public Long getCategorieActive() { return categorieActive; }
    public List<ServiceVitrineDto> getServices() { return services; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long v) { this.serviceId = v; }
    public ServiceDetailDto getDetail() { return detail; }

    public int getMaNote() { return maNote; }
    public void setMaNote(int v) { this.maNote = v; }
    public String getMonCommentaire() { return monCommentaire; }
    public void setMonCommentaire(String v) { this.monCommentaire = v; }
    public AvisDto getMonAvis() { return monAvis; }
}
