package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** Compteurs affichés sur le tableau de bord d'administration. */
@Data
public class TableauBordDto implements Serializable {

    // --- Utilisateurs & sécurité ---
    private long personnes;
    private long personnesActives;
    private long personnesVerrouillees;
    private long personnesEnAttente;
    private long profils;
    private long permissions;
    private long branches;

    // --- Plateforme : offre ---
    private long services;
    private long servicesGeolocalises;
    private long servicesEnAttente;
    private long servicesBloques;
    private long fournisseurs;
    private long clients;
    private long favoris;

    // --- Commandes ---
    private long commandes;
    private long commandesATraiter;
    private BigDecimal montantCommandes = BigDecimal.ZERO;

    // --- Avis & signalements ---
    private long avisPublies;
    private long avisEnAttente;
    private long signalementsNouveaux;

    /** % d'avis déjà modérés (publiés ou rejetés) sur le total déposé — pour la jauge. */
    public double getTauxAvisModeres() {
        long total = avisPublies + avisEnAttente;
        return total == 0 ? 100 : Math.round(avisPublies * 1000.0 / total) / 10.0;
    }

    /** Nombre total d'actions en attente d'un administrateur (file « à traiter »). */
    public long getTotalATraiter() {
        return personnesEnAttente + servicesEnAttente + avisEnAttente + signalementsNouveaux + commandesATraiter;
    }
}
