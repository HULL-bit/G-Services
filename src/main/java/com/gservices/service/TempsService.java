package com.gservices.service;

import com.gservices.dto.AnneeDto;
import com.gservices.dto.JourDto;
import com.gservices.dto.MoisDto;

import java.util.List;

/** Référentiel « temps » : jours de la semaine, années calendaires et leurs mois. */
public interface TempsService {

    // --- Jours (référentiel figé : lundi … dimanche) ---
    List<JourDto> jours();
    JourDto modifierJour(Long id, JourDto dto);
    void basculerJour(Long id);

    // --- Années ---
    List<AnneeDto> annees();
    AnneeDto annee(Long id);

    /** Crée une année et génère automatiquement ses 12 mois (28/29 février géré). */
    AnneeDto creerAnnee(int valeurAnnee);
    void basculerAnnee(Long id);

    // --- Mois ---
    List<MoisDto> moisParAnnee(Long idAnnee);
    MoisDto modifierMois(Long id, MoisDto dto);
    void basculerMois(Long id);
}
