package com.gservices.web.bean;

import com.gservices.dto.*;
import com.gservices.service.StockService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Écran unique du module Stock &amp; prestataires : onglets Stocks, Fiches
 * prestataires. Lots et horaires se gèrent depuis une ligne (dialogue).
 */
@Component("stockBean")
@Scope("view")
public class StockBean extends AbstractAdminBean {

    private final transient StockService service;

    private String filtreStock;
    private Long filtreFournisseurStock;
    private String filtrePrestataire;
    private List<PersonneDto> refFournisseursStock = new ArrayList<>();

    private LazyDataModel<StockDto> stocks;
    private LazyDataModel<InformationServiceDto> prestataires;

    private StockDto stock = new StockDto();
    private InformationServiceDto prestataire = new InformationServiceDto();

    private List<ArticleDto> articlesSansStock = new ArrayList<>();
    private List<ServiceDto> servicesSansFiche = new ArrayList<>();
    private List<PositionDto> positions = new ArrayList<>();
    private List<JourDto> jours = new ArrayList<>();
    private List<AnneeDto> annees = new ArrayList<>();

    // --- Lots d'un stock ---
    private StockDto stockLots;
    private List<LotDto> lots = new ArrayList<>();
    private LotDto lot = new LotDto();
    private Long lotAnneeId;
    private List<MoisDto> lotMois = new ArrayList<>();

    // --- Horaires d'un prestataire ---
    private InformationServiceDto prestataireHoraires;
    private List<HoraireDto> horaires = new ArrayList<>();
    private HoraireDto horaire = new HoraireDto();

    public StockBean(StockService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        stocks = new LazyModelService<>(
                (f, pageable) -> service.rechercherStocks(f, filtreFournisseurStock, pageable),
                StockDto::getId, () -> filtreStock);
        prestataires = new LazyModelService<>(service::rechercherPrestataires, InformationServiceDto::getId, () -> filtrePrestataire);
        recharger();
    }

    private void recharger() {
        articlesSansStock = safe(service::articlesSansStock);
        servicesSansFiche = safe(service::servicesSansFiche);
        positions = safe(service::positionsActives);
        jours = safe(service::jours);
        annees = safe(service::anneesActives);
        refFournisseursStock = safe(service::fournisseursAvecStock);
    }

    // ================================================= Stock ================

    public void nouveauStock() {
        stock = new StockDto();
        articlesSansStock = safe(service::articlesSansStock);
    }

    public void editerStock(StockDto d) {
        stock = service.stock(d.getId());
    }

    public void enregistrerStock() {
        boolean ok = executer(() -> {
            if (stock.getId() == null) {
                service.ouvrirStock(stock);
            } else {
                service.modifierStock(stock.getId(), stock);
            }
            recharger();
        });
        termine(ok, "stock.stock.enregistre");
    }

    public void basculerStock(StockDto d) {
        if (executer(() -> service.basculerStock(d.getId()))) {
            callback("ok", true);
        }
    }

    // ---- Lots ----

    public void gererLots(StockDto d) {
        boolean ok = executer(() -> {
            stockLots = service.stock(d.getId());
            lots = service.lotsParStock(d.getId());
            preparerLot();
        });
        if (ok) {
            callback("lots", true);
        }
    }

    public void preparerLot() {
        lot = new LotDto();
        lotAnneeId = null;
        lotMois = new ArrayList<>();
    }

    public void editerLot(LotDto d) {
        lot = d;
        lotAnneeId = null;
        // retrouve l'année du mois pour pré-remplir la cascade
        for (AnneeDto a : annees) {
            if (a.getValeurAnnee() != null && a.getValeurAnnee().equals(d.getAnneeValeur())) {
                lotAnneeId = a.getId();
                lotMois = safe(() -> service.moisParAnnee(a.getId()));
                break;
            }
        }
    }

    public void onLotAnnee() {
        lot.setMoisId(null);
        lotMois = lotAnneeId == null ? new ArrayList<>() : safe(() -> service.moisParAnnee(lotAnneeId));
    }

    public void enregistrerLot() {
        boolean ok = executer(() -> {
            service.enregistrerLot(stockLots.getId(), lot);
            stockLots = service.stock(stockLots.getId());
            lots = service.lotsParStock(stockLots.getId());
            preparerLot();
        });
        if (ok) {
            info(msg("stock.lot.enregistre"));
            callback("oklot", true);
        }
    }

    public void basculerLot(LotDto d) {
        if (executer(() -> service.basculerLot(d.getId()))) {
            stockLots = service.stock(stockLots.getId());
            lots = service.lotsParStock(stockLots.getId());
            callback("oklot", true);
        }
    }

    // ================================================= Prestataire ==========

    public void nouveauPrestataire() {
        prestataire = new InformationServiceDto();
        servicesSansFiche = safe(service::servicesSansFiche);
    }

    public void editerPrestataire(InformationServiceDto d) {
        prestataire = service.prestataire(d.getId());
    }

    public void enregistrerPrestataire() {
        boolean ok = executer(() -> {
            if (prestataire.getId() == null) {
                service.creerPrestataire(prestataire);
            } else {
                service.modifierPrestataire(prestataire.getId(), prestataire);
            }
            recharger();
        });
        termine(ok, "stock.prestataire.enregistre");
    }

    public void basculerPrestataire(InformationServiceDto d) {
        if (executer(() -> service.basculerPrestataire(d.getId()))) {
            callback("ok", true);
        }
    }

    // ---- Horaires ----

    public void gererHoraires(InformationServiceDto d) {
        boolean ok = executer(() -> {
            prestataireHoraires = service.prestataire(d.getId());
            horaires = service.horairesParPrestataire(d.getId());
            horaire = new HoraireDto();
        });
        if (ok) {
            callback("horaires", true);
        }
    }

    public void editerHoraire(HoraireDto d) {
        this.horaire = d;
    }

    public void enregistrerHoraire() {
        boolean ok = executer(() -> {
            service.enregistrerHoraire(prestataireHoraires.getId(), horaire);
            horaires = service.horairesParPrestataire(prestataireHoraires.getId());
            horaire = new HoraireDto();
        });
        if (ok) {
            info(msg("stock.horaire.enregistre"));
            callback("okhor", true);
        }
    }

    public void basculerHoraire(HoraireDto d) {
        if (executer(() -> service.basculerHoraire(d.getId()))) {
            horaires = service.horairesParPrestataire(prestataireHoraires.getId());
            callback("okhor", true);
        }
    }

    // ================================================= util =================

    private void termine(boolean ok, String cle) {
        if (ok) {
            info(msg(cle));
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

    // ------------------------------------------------- accessors ------------

    public String getFiltreStock() { return filtreStock; }
    public void setFiltreStock(String v) { this.filtreStock = v; }
    public Long getFiltreFournisseurStock() { return filtreFournisseurStock; }
    public void setFiltreFournisseurStock(Long v) { this.filtreFournisseurStock = v; }
    public List<PersonneDto> getRefFournisseursStock() { return refFournisseursStock; }
    public String getFiltrePrestataire() { return filtrePrestataire; }
    public void setFiltrePrestataire(String v) { this.filtrePrestataire = v; }

    public LazyDataModel<StockDto> getStocks() { return stocks; }
    public LazyDataModel<InformationServiceDto> getPrestataires() { return prestataires; }

    public StockDto getStock() { return stock; }
    public void setStock(StockDto v) { this.stock = v; }
    public InformationServiceDto getPrestataire() { return prestataire; }
    public void setPrestataire(InformationServiceDto v) { this.prestataire = v; }

    public List<ArticleDto> getArticlesSansStock() { return articlesSansStock; }
    public List<ServiceDto> getServicesSansFiche() { return servicesSansFiche; }
    public List<PositionDto> getPositions() { return positions; }
    public List<JourDto> getJours() { return jours; }
    public List<AnneeDto> getAnnees() { return annees; }

    public StockDto getStockLots() { return stockLots; }
    public List<LotDto> getLots() { return lots; }
    public LotDto getLot() { return lot; }
    public void setLot(LotDto v) { this.lot = v; }
    public Long getLotAnneeId() { return lotAnneeId; }
    public void setLotAnneeId(Long v) { this.lotAnneeId = v; }
    public List<MoisDto> getLotMois() { return lotMois; }

    public InformationServiceDto getPrestataireHoraires() { return prestataireHoraires; }
    public List<HoraireDto> getHoraires() { return horaires; }
    public HoraireDto getHoraire() { return horaire; }
    public void setHoraire(HoraireDto v) { this.horaire = v; }
}
