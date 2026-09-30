package com.gservices.web.bean;

import com.gservices.dto.AnneeDto;
import com.gservices.dto.JourDto;
import com.gservices.dto.MoisDto;
import com.gservices.service.TempsService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/** Écran d'administration du référentiel « temps » : jours, années et mois. */
@Component("tempsBean")
@Scope("view")
public class TempsBean extends AbstractAdminBean {

    private final transient TempsService service;

    private List<JourDto> jours = new ArrayList<>();
    private JourDto jour = new JourDto();

    private List<AnneeDto> annees = new ArrayList<>();
    private AnneeDto anneeSelection;
    private List<MoisDto> moisSelection = new ArrayList<>();
    private int nouvelleAnnee = Year.now().getValue() + 1;

    // Onglet « Mois » : année sélectionnée + liste + édition
    private Long moisAnneeId;
    private List<MoisDto> moisAnnee = new ArrayList<>();
    private MoisDto moisSel = new MoisDto();

    public TempsBean(TempsService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        recharger();
    }

    public void recharger() {
        try {
            jours = service.jours();
            annees = service.annees();
        } catch (RuntimeException e) {
            jours = new ArrayList<>();
            annees = new ArrayList<>();
        }
        if (moisAnneeId == null && !annees.isEmpty()) {
            moisAnneeId = annees.get(0).getId();
        }
        chargerMoisAnnee();
    }

    // --- Onglet Mois ---

    public void chargerMoisAnnee() {
        try {
            moisAnnee = moisAnneeId == null ? new ArrayList<>() : service.moisParAnnee(moisAnneeId);
        } catch (RuntimeException e) {
            moisAnnee = new ArrayList<>();
        }
    }

    public void editerMois(MoisDto m) {
        moisSel = m;
    }

    public void enregistrerMois() {
        boolean ok = executer(() -> {
            service.modifierMois(moisSel.getId(), moisSel);
            chargerMoisAnnee();
        });
        if (ok) {
            info(msg("temps.mois.enregistre"));
            callback("ok", true);
        }
    }

    public void basculerMoisTab(MoisDto m) {
        if (executer(() -> service.basculerMois(m.getId()))) {
            chargerMoisAnnee();
        }
    }

    // --- Jours ---

    public void editerJour(JourDto d) {
        jour = d;
    }

    public void enregistrerJour() {
        boolean ok = executer(() -> {
            service.modifierJour(jour.getId(), jour);
            jours = service.jours();
        });
        if (ok) {
            info(msg("temps.jour.enregistre"));
            callback("ok", true);
        }
    }

    public void basculerJour(JourDto d) {
        if (executer(() -> service.basculerJour(d.getId()))) {
            jours = service.jours();
        }
    }

    // --- Années / mois ---

    public void creerAnnee() {
        boolean ok = executer(() -> {
            service.creerAnnee(nouvelleAnnee);
            annees = service.annees();
        });
        if (ok) {
            info(msg("temps.annee.creee"));
            callback("ok", true);
        }
    }

    public void basculerAnnee(AnneeDto d) {
        if (executer(() -> service.basculerAnnee(d.getId()))) {
            annees = service.annees();
        }
    }

    public void voirMois(AnneeDto d) {
        boolean ok = executer(() -> {
            anneeSelection = d;
            moisSelection = service.moisParAnnee(d.getId());
        });
        if (ok) {
            callback("mois", true);
        }
    }

    public void basculerMois(MoisDto m) {
        if (executer(() -> service.basculerMois(m.getId()))) {
            moisSelection = service.moisParAnnee(anneeSelection.getId());
        }
    }

    // --- getters ---

    public List<JourDto> getJours() { return jours; }
    public JourDto getJour() { return jour; }
    public void setJour(JourDto v) { this.jour = v; }
    public List<AnneeDto> getAnnees() { return annees; }
    public AnneeDto getAnneeSelection() { return anneeSelection; }
    public List<MoisDto> getMoisSelection() { return moisSelection; }
    public int getNouvelleAnnee() { return nouvelleAnnee; }
    public void setNouvelleAnnee(int v) { this.nouvelleAnnee = v; }
    public Long getMoisAnneeId() { return moisAnneeId; }
    public void setMoisAnneeId(Long v) { this.moisAnneeId = v; }
    public List<MoisDto> getMoisAnnee() { return moisAnnee; }
    public MoisDto getMoisSel() { return moisSel; }
    public void setMoisSel(MoisDto v) { this.moisSel = v; }
}
