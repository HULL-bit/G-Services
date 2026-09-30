package com.gservices.web.bean;

import com.gservices.dto.*;
import com.gservices.service.GeographieService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Écran d'administration du référentiel géographique (onglet unique, hiérarchie
 * {@code Continent → ZoneGeographique → Pays → Region → Ville → Position}).
 * Le formulaire Ville démontre les <strong>listes en cascade</strong>
 * Continent → Pays → Région ; les tableaux sont paginés côté serveur.
 */
@Component("geographieBean")
@Scope("view")
public class GeographieBean extends AbstractAdminBean {

    private final transient GeographieService service;

    // Filtres texte
    private String filtreContinent;
    private String filtreZone;
    private String filtrePays;
    private String filtreRegion;
    private String filtreVille;

    // Modèles paginés
    private LazyDataModel<ContinentDto> continents;
    private LazyDataModel<ZoneGeographiqueDto> zones;
    private LazyDataModel<PaysDto> pays;
    private LazyDataModel<RegionDto> regions;
    private LazyDataModel<VilleDto> villes;

    // Sélections d'édition
    private ContinentDto continent = new ContinentDto();
    private ZoneGeographiqueDto zone = new ZoneGeographiqueDto();
    private PaysDto unPays = new PaysDto();
    private RegionDto region = new RegionDto();
    private VilleDto ville = new VilleDto();
    private PositionDto position = new PositionDto();

    // Référentiels plats pour les listes déroulantes des formulaires
    private List<ContinentDto> refContinents = new ArrayList<>();
    private List<ZoneGeographiqueDto> refZones = new ArrayList<>();
    private List<PaysDto> refPays = new ArrayList<>();

    // Cascade du formulaire Ville : Continent → Pays → Région
    private Long villeContinentId;
    private Long villePaysId;
    private List<PaysDto> cascadePays = new ArrayList<>();
    private List<RegionDto> cascadeRegions = new ArrayList<>();

    // Gestion des positions d'une ville
    private VilleDto villePositions;
    private List<PositionDto> positionsVille = new ArrayList<>();

    // Carte
    private List<VilleDto> villesCarte = new ArrayList<>();

    public GeographieBean(GeographieService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        continents = new LazyModelService<>(service::rechercherContinents, ContinentDto::getId, () -> filtreContinent);
        zones = new LazyModelService<>(service::rechercherZones, ZoneGeographiqueDto::getId, () -> filtreZone);
        pays = new LazyModelService<>(service::rechercherPays, PaysDto::getId, () -> filtrePays);
        regions = new LazyModelService<>(service::rechercherRegions, RegionDto::getId, () -> filtreRegion);
        villes = new LazyModelService<>(service::rechercherVilles, VilleDto::getId, () -> filtreVille);
        rechargerReferentiels();
    }

    private void rechargerReferentiels() {
        refContinents = safe(service::continentsActifs);
        refZones = safe(service::zonesActives);
        refPays = safe(service::paysActifs);
    }

    // ============================================================ Continent ===

    public void nouveauContinent() {
        continent = new ContinentDto();
    }

    public void editerContinent(ContinentDto d) {
        continent = service.continent(d.getId());
    }

    public void enregistrerContinent() {
        boolean ok = executer(() -> {
            if (continent.getId() == null) {
                service.creerContinent(continent);
            } else {
                service.modifierContinent(continent.getId(), continent);
            }
            rechargerReferentiels();
        });
        termine(ok, "geo.continent.enregistre");
    }

    public void basculerContinent(ContinentDto d) {
        if (executer(() -> service.basculerContinent(d.getId()))) {
            rechargerReferentiels();
        }
    }

    // ============================================================ Zone ========

    public void nouvelleZone() {
        zone = new ZoneGeographiqueDto();
    }

    public void editerZone(ZoneGeographiqueDto d) {
        zone = d;
    }

    public void enregistrerZone() {
        boolean ok = executer(() -> {
            if (zone.getId() == null) {
                service.creerZone(zone);
            } else {
                service.modifierZone(zone.getId(), zone);
            }
            rechargerReferentiels();
        });
        termine(ok, "geo.zone.enregistre");
    }

    public void basculerZone(ZoneGeographiqueDto d) {
        if (executer(() -> service.basculerZone(d.getId()))) {
            rechargerReferentiels();
        }
    }

    // ============================================================ Pays ========

    public void nouveauPays() {
        unPays = new PaysDto();
    }

    public void editerPays(PaysDto d) {
        unPays = d;
    }

    public void enregistrerPays() {
        boolean ok = executer(() -> {
            if (unPays.getId() == null) {
                service.creerPays(unPays);
            } else {
                service.modifierPays(unPays.getId(), unPays);
            }
            rechargerReferentiels();
        });
        termine(ok, "geo.pays.enregistre");
    }

    public void basculerPays(PaysDto d) {
        if (executer(() -> service.basculerPays(d.getId()))) {
            rechargerReferentiels();
        }
    }

    // ============================================================ Region ======

    public void nouvelleRegion() {
        region = new RegionDto();
    }

    public void editerRegion(RegionDto d) {
        region = d;
    }

    public void enregistrerRegion() {
        boolean ok = executer(() -> {
            if (region.getId() == null) {
                service.creerRegion(region);
            } else {
                service.modifierRegion(region.getId(), region);
            }
        });
        termine(ok, "geo.region.enregistre");
    }

    public void basculerRegion(RegionDto d) {
        executer(() -> service.basculerRegion(d.getId()));
        callback("ok", true);
    }

    // ============================================================ Ville =======

    public void nouvelleVille() {
        ville = new VilleDto();
        villeContinentId = null;
        villePaysId = null;
        cascadePays = new ArrayList<>();
        cascadeRegions = new ArrayList<>();
    }

    public void editerVille(VilleDto d) {
        ville = service.ville(d.getId());
        villeContinentId = ville.getContinentId();
        villePaysId = ville.getPaysId();
        cascadePays = villeContinentId == null ? new ArrayList<>() : safe(() -> service.paysParContinent(villeContinentId));
        cascadeRegions = villePaysId == null ? new ArrayList<>() : safe(() -> service.regionsParPays(villePaysId));
    }

    /** Étape 1 de la cascade : le continent choisi filtre la liste des pays. */
    public void onCascadeContinent() {
        villePaysId = null;
        ville.setRegionId(null);
        cascadeRegions = new ArrayList<>();
        cascadePays = villeContinentId == null ? new ArrayList<>() : safe(() -> service.paysParContinent(villeContinentId));
    }

    /** Étape 2 de la cascade : le pays choisi filtre la liste des régions. */
    public void onCascadePays() {
        ville.setRegionId(null);
        cascadeRegions = villePaysId == null ? new ArrayList<>() : safe(() -> service.regionsParPays(villePaysId));
    }

    public void enregistrerVille() {
        boolean ok = executer(() -> {
            if (ville.getId() == null) {
                service.creerVille(ville);
            } else {
                service.modifierVille(ville.getId(), ville);
            }
        });
        termine(ok, "geo.ville.enregistre");
    }

    public void basculerVille(VilleDto d) {
        executer(() -> service.basculerVille(d.getId()));
        callback("ok", true);
    }

    // ============================================================ Positions ===

    public void gererPositions(VilleDto d) {
        boolean ok = executer(() -> {
            villePositions = service.ville(d.getId());
            positionsVille = service.positionsParVille(d.getId());
            preparerPosition();
        });
        if (ok) {
            callback("positions", true);
        }
    }

    public void preparerPosition() {
        position = new PositionDto();
        position.setVilleId(villePositions == null ? null : villePositions.getId());
    }

    public void editerPosition(PositionDto d) {
        position = d;
    }

    public void enregistrerPosition() {
        boolean ok = executer(() -> {
            service.enregistrerPosition(villePositions.getId(), position);
            positionsVille = service.positionsParVille(villePositions.getId());
            preparerPosition();
        });
        if (ok) {
            info(msg("geo.position.enregistre"));
            callback("okpos", true);
        }
    }

    public void basculerPosition(PositionDto d) {
        if (executer(() -> service.basculerPosition(d.getId()))) {
            positionsVille = service.positionsParVille(villePositions.getId());
            callback("okpos", true);
        }
    }

    // ============================================================ Carte =======

    public void chargerCarte() {
        villesCarte = safe(service::villesGeolocalisees);
    }

    // ============================================================ util ========

    private void termine(boolean ok, String cleMessage) {
        if (ok) {
            info(msg(cleMessage));
            callback("ok", true);
        }
    }

    private static <T> List<T> safe(Supplier<List<T>> s) {
        try {
            return s.get();
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    // ------------------------------------------------------------- accessors ---

    public String getFiltreContinent() { return filtreContinent; }
    public void setFiltreContinent(String v) { this.filtreContinent = v; }
    public String getFiltreZone() { return filtreZone; }
    public void setFiltreZone(String v) { this.filtreZone = v; }
    public String getFiltrePays() { return filtrePays; }
    public void setFiltrePays(String v) { this.filtrePays = v; }
    public String getFiltreRegion() { return filtreRegion; }
    public void setFiltreRegion(String v) { this.filtreRegion = v; }
    public String getFiltreVille() { return filtreVille; }
    public void setFiltreVille(String v) { this.filtreVille = v; }

    public LazyDataModel<ContinentDto> getContinents() { return continents; }
    public LazyDataModel<ZoneGeographiqueDto> getZones() { return zones; }
    public LazyDataModel<PaysDto> getPays() { return pays; }
    public LazyDataModel<RegionDto> getRegions() { return regions; }
    public LazyDataModel<VilleDto> getVilles() { return villes; }

    public ContinentDto getContinent() { return continent; }
    public void setContinent(ContinentDto v) { this.continent = v; }
    public ZoneGeographiqueDto getZone() { return zone; }
    public void setZone(ZoneGeographiqueDto v) { this.zone = v; }
    public PaysDto getUnPays() { return unPays; }
    public void setUnPays(PaysDto v) { this.unPays = v; }
    public RegionDto getRegion() { return region; }
    public void setRegion(RegionDto v) { this.region = v; }
    public VilleDto getVille() { return ville; }
    public void setVille(VilleDto v) { this.ville = v; }
    public PositionDto getPosition() { return position; }
    public void setPosition(PositionDto v) { this.position = v; }

    public List<ContinentDto> getRefContinents() { return refContinents; }
    public List<ZoneGeographiqueDto> getRefZones() { return refZones; }
    public List<PaysDto> getRefPays() { return refPays; }

    public Long getVilleContinentId() { return villeContinentId; }
    public void setVilleContinentId(Long v) { this.villeContinentId = v; }
    public Long getVillePaysId() { return villePaysId; }
    public void setVillePaysId(Long v) { this.villePaysId = v; }
    public List<PaysDto> getCascadePays() { return cascadePays; }
    public List<RegionDto> getCascadeRegions() { return cascadeRegions; }

    public VilleDto getVillePositions() { return villePositions; }
    public List<PositionDto> getPositionsVille() { return positionsVille; }
    public List<VilleDto> getVillesCarte() { return villesCarte; }
}
