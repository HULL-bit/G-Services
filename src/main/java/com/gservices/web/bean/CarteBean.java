package com.gservices.web.bean;

import com.gservices.dto.CategorieServiceDto;
import com.gservices.dto.ServiceVitrineDto;
import com.gservices.service.VitrineService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Page publique « Carte » (<code>/public/carte.xhtml</code>) : tous les
 * prestataires géolocalisés de Dakar. Le filtrage (texte, catégorie, note
 * minimale, disponibilité) est fait <strong>côté client</strong> dans
 * {@code carte.js} à partir du JSON exposé ici — instantané, sans aller-retour.
 */
@Component("carteBean")
@Scope("view")
public class CarteBean implements Serializable {

    private final transient VitrineService vitrine;
    private List<ServiceVitrineDto> services = new ArrayList<>();
    private List<CategorieServiceDto> categories = new ArrayList<>();

    public CarteBean(VitrineService vitrine) {
        this.vitrine = vitrine;
    }

    @PostConstruct
    void init() {
        try {
            services = vitrine.servicesGeolocalises();
            categories = vitrine.categories();
        } catch (RuntimeException e) {
            services = new ArrayList<>();
            categories = new ArrayList<>();
        }
    }

    public int getNombre() {
        return services.size();
    }

    public List<CategorieServiceDto> getCategories() {
        return categories;
    }

    /** JSON des points, injecté tel quel dans un {@code <script>}. */
    public String getServicesJson() {
        String ctx = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < services.size(); i++) {
            ServiceVitrineDto s = services.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"id\":").append(s.getId())
              .append(",\"libelle\":\"").append(js(s.getLibelle())).append('"')
              .append(",\"categorie\":\"").append(js(s.getCategorieLibelle())).append('"')
              .append(",\"icone\":\"").append(js(s.getCategorieIcone())).append('"')
              .append(",\"adresse\":\"").append(js(s.getAdresse())).append('"')
              .append(",\"lat\":").append(s.getLatitude())
              .append(",\"lng\":").append(s.getLongitude())
              .append(",\"note\":").append(round1(s.getNoteMoyenne()))
              .append(",\"avis\":").append(s.getNombreAvis())
              .append(",\"dispo\":").append(s.isDisponible())
              .append(",\"url\":\"").append(ctx).append("/public/service.xhtml?id=").append(s.getId()).append("\"}");
        }
        return sb.append(']').toString();
    }

    private static double round1(double d) {
        return Math.round(d * 10.0) / 10.0;
    }

    private static String js(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("<", "\\u003c").replace("\n", " ").replace("\r", " ");
    }
}
