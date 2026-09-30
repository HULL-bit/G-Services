package com.gservices.web.bean;

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
 * Bean de la page d'accueil publique.
 *
 * <p>La <strong>carte</strong> ({@link #getPointsCarteJson()}) est alimentée par
 * les prestataires réellement géolocalisés. Les indicateurs, étapes et visuels
 * du carrousel restent des valeurs d'ambiance (le carrousel s'appuie sur les
 * images du dossier {@code /media}).</p>
 */
@Component("homeBean")
@Scope("view")
public class HomeBean implements Serializable {

    private final transient VitrineService vitrineService;

    private List<Indicateur> indicateurs;
    private List<Etape> etapes;
    private List<CarteVitrine> vitrine;
    /** Prestataires géolocalisés pour la carte de l'accueil. */
    private List<ServiceVitrineDto> pointsCarte = new ArrayList<>();

    public HomeBean(VitrineService vitrineService) {
        this.vitrineService = vitrineService;
    }

    @PostConstruct
    void init() {
        try {
            pointsCarte = vitrineService.servicesGeolocalises();
        } catch (RuntimeException e) {
            pointsCarte = new ArrayList<>();
        }
        indicateurs = List.of(
            new Indicateur("pi pi-briefcase",  "1280", 0, "+",  "home.kpi.services"),
            new Indicateur("pi pi-globe",      "42",   0, "",   "home.kpi.pays"),
            new Indicateur("pi pi-users",      "870",  0, "+",  "home.kpi.prestataires"),
            new Indicateur("pi pi-star-fill",  "4.8",  1, "/5", "home.kpi.satisfaction")
        );

        etapes = List.of(
            new Etape("01", "pi pi-map-marker", "home.step1.title", "home.step1.text"),
            new Etape("02", "pi pi-search",     "home.step2.title", "home.step2.text"),
            new Etape("03", "pi pi-check-circle","home.step3.title", "home.step3.text")
        );

        // image = nom de ressource JSF (dossier src/main/webapp/resources/media/…)
        vitrine = List.of(
            new CarteVitrine("home.slide1.title", "home.slide1.cat", "Dakar",     4.9, "media/carousel/city-navigation.jpg"),
            new CarteVitrine("home.slide2.title", "home.slide2.cat", "Abidjan",   4.7, "media/carousel/on-the-road.jpg"),
            new CarteVitrine("home.slide3.title", "home.slide3.cat", "Casablanca",4.8, "media/carousel/cafe-discovery.jpg"),
            new CarteVitrine("home.slide4.title", "home.slide4.cat", "Paris",     4.6, "media/backgrounds/discover-map.jpg")
        );
    }

    public List<Indicateur> getIndicateurs() { return indicateurs; }
    public List<Etape> getEtapes()           { return etapes; }
    public List<CarteVitrine> getVitrine()   { return vitrine; }
    public List<ServiceVitrineDto> getPointsCarte() { return pointsCarte; }
    public boolean isCarteDisponible() { return !pointsCarte.isEmpty(); }

    /** Tableau JSON des points, prêt à être injecté dans un {@code <script>} (voir accueil). */
    public String getPointsCarteJson() {
        StringBuilder sb = new StringBuilder("[");
        String ctx = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        for (int i = 0; i < pointsCarte.size(); i++) {
            ServiceVitrineDto p = pointsCarte.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"lat\":").append(p.getLatitude())
              .append(",\"lng\":").append(p.getLongitude())
              .append(",\"libelle\":\"").append(js(p.getLibelle())).append('"')
              .append(",\"pays\":\"").append(js(p.getVilleLibelle())).append('"')
              .append(",\"url\":\"").append(ctx).append("/public/service.xhtml?id=").append(p.getId()).append("\"}");
        }
        return sb.append(']').toString();
    }

    private static String js(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("<", "\\u003c");
    }

    /** Année courante — pied de page. */
    public int getAnneeCourante() { return java.time.Year.now().getValue(); }


    // ----------------------------------------------------------------- modèles

    /**
     * Indicateur clé animé (count-up).
     * @param cible     valeur cible telle qu'affichée ({@code "1280"}, {@code "4.8"})
     * @param decimales nombre de décimales pour l'animation JS
     * @param suffixe   suffixe collé à la valeur ({@code "+"}, {@code "/5"})
     * @param cle       clé i18n du libellé
     */
    public static class Indicateur implements Serializable {
        private final String icone;
        private final String cible;
        private final int decimales;
        private final String suffixe;
        private final String cle;

        public Indicateur(String icone, String cible, int decimales, String suffixe, String cle) {
            this.icone = icone; this.cible = cible; this.decimales = decimales;
            this.suffixe = suffixe; this.cle = cle;
        }
        public String getIcone()    { return icone; }
        public String getCible()    { return cible; }
        public int    getDecimales(){ return decimales; }
        public String getSuffixe()  { return suffixe; }
        public String getCle()      { return cle; }
    }

    /** Étape du parcours « Comment ça marche ». */
    public static class Etape implements Serializable {
        private final String numero;
        private final String icone;
        private final String cleTitre;
        private final String cleTexte;

        public Etape(String numero, String icone, String cleTitre, String cleTexte) {
            this.numero = numero; this.icone = icone; this.cleTitre = cleTitre; this.cleTexte = cleTexte;
        }
        public String getNumero()   { return numero; }
        public String getIcone()    { return icone; }
        public String getCleTitre() { return cleTitre; }
        public String getCleTexte() { return cleTexte; }
    }

    /** Carte de la vitrine défilante (carrousel). */
    public static class CarteVitrine implements Serializable {
        private final String cleTitre;
        private final String cleCategorie;
        private final String ville;
        private final double note;
        private final String image;

        public CarteVitrine(String cleTitre, String cleCategorie, String ville, double note, String image) {
            this.cleTitre = cleTitre; this.cleCategorie = cleCategorie;
            this.ville = ville; this.note = note; this.image = image;
        }
        public String getCleTitre()     { return cleTitre; }
        public String getCleCategorie() { return cleCategorie; }
        public String getVille()        { return ville; }
        public double getNote()         { return note; }
        public String getImage()        { return image; }
    }
}
