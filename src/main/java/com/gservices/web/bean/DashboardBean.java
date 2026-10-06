package com.gservices.web.bean;

import com.gservices.dto.PersonneDto;
import com.gservices.dto.RepartitionDto;
import com.gservices.dto.TableauBordDto;
import com.gservices.security.SecurityUtils;
import com.gservices.service.TableauBordService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;
import org.primefaces.model.charts.donut.DonutChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Tableau de bord : vue d'ensemble de la plateforme (offre, fournisseurs,
 * commandes, avis, signalements) + volet sécurité (comptes/permissions).
 */
@Component("dashboardBean")
@RequestScope
public class DashboardBean implements Serializable {

    /** Palette de marque (identique au front public) pour tous les graphiques. */
    private static final String[] PALETTE = {
            "#1F3A5F", "#C9A227", "#2F6FED", "#2F855A", "#E63946", "#7B61FF", "#E0A458", "#64748B"
    };

    private final transient TableauBordService service;

    private TableauBordDto chiffres;
    private List<RepartitionDto> repartition;
    private List<PersonneDto> comptesVerrouilles;
    private DonutChartModel servicesParCategorie;
    private BarChartModel commandesParStatut;

    public DashboardBean(TableauBordService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        this.chiffres = service.chiffres();
        this.repartition = service.repartitionPermissions();
        this.comptesVerrouilles = service.comptesVerrouilles();
        this.servicesParCategorie = construireDonut(service.repartitionServices());
        this.commandesParStatut = construireBarres(service.repartitionCommandes());
    }

    public String getNomComplet() {
        return SecurityUtils.currentUser().map(u -> u.getNomComplet()).orElse(null);
    }

    public TableauBordDto getChiffres() { return chiffres; }
    public List<RepartitionDto> getRepartition() { return repartition; }
    public List<PersonneDto> getComptesVerrouilles() { return comptesVerrouilles; }
    public DonutChartModel getServicesParCategorie() { return servicesParCategorie; }
    public BarChartModel getCommandesParStatut() { return commandesParStatut; }

    private DonutChartModel construireDonut(List<RepartitionDto> source) {
        DonutChartModel model = new DonutChartModel();
        ChartData data = new ChartData();
        DonutChartDataSet dataSet = new DonutChartDataSet();

        List<Number> valeurs = new ArrayList<>();
        List<String> libelles = new ArrayList<>();
        List<String> couleurs = new ArrayList<>();
        int i = 0;
        for (RepartitionDto r : source) {
            valeurs.add(r.getValeur());
            libelles.add(r.getLibelle());
            couleurs.add(PALETTE[i % PALETTE.length]);
            i++;
        }
        dataSet.setData(valeurs);
        dataSet.setBackgroundColor(couleurs);
        data.addChartDataSet(dataSet);
        data.setLabels(libelles);
        model.setData(data);

        DonutChartOptions options = new DonutChartOptions();
        Legend legend = new Legend();
        legend.setPosition("bottom");
        options.setLegend(legend);
        model.setOptions(options);
        return model;
    }

    private BarChartModel construireBarres(List<RepartitionDto> source) {
        BarChartModel model = new BarChartModel();
        ChartData data = new ChartData();
        BarChartDataSet dataSet = new BarChartDataSet();

        List<Object> valeurs = new ArrayList<>();
        List<String> libelles = new ArrayList<>();
        List<Object> couleurs = new ArrayList<>();
        int i = 0;
        for (RepartitionDto r : source) {
            valeurs.add(r.getValeur());
            libelles.add(msg("commande.statut." + r.getLibelle()));
            couleurs.add(PALETTE[i % PALETTE.length]);
            i++;
        }
        dataSet.setLabel(msg("dash.commandes.parStatut"));
        dataSet.setData(valeurs);
        dataSet.setBackgroundColor(couleurs);
        data.addChartDataSet(dataSet);
        data.setLabels(libelles);
        model.setData(data);

        BarChartOptions options = new BarChartOptions();
        Legend legend = new Legend();
        legend.setDisplay(false);
        options.setLegend(legend);
        model.setOptions(options);
        return model;
    }

    private static String msg(String cle) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getApplication().evaluateExpressionGet(ctx, "#{msg['" + cle + "']}", String.class);
    }
}
