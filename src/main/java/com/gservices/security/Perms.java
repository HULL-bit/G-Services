package com.gservices.security;

/**
 * Codes de permission (autorités Spring Security) — schéma numérique
 * hiérarchique : {@code <code branche> + rang}.
 *
 * <p>Branche 1000 = Tableau de bord · Branche 2000 = Utilisateurs &amp; sécurité.
 * Les permissions créées via l'IHM pour d'autres branches suivront le même
 * principe (3001, 3002… pour la branche 3000, etc.).</p>
 *
 * <p>Utilisation : {@code @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).PERSONNE_LIRE)")}.</p>
 */
public final class Perms {

    private Perms() {
    }

    // --- Branche 1000 : Tableau de bord ---
    public static final String DASHBOARD_LIRE = "1001";

    // --- Branche 2000 : Utilisateurs & sécurité ---
    public static final String PERSONNE_LIRE      = "2001";
    public static final String PERSONNE_CREER     = "2002";
    public static final String PERSONNE_MODIFIER  = "2003";
    public static final String PERSONNE_SUPPRIMER = "2004";
    public static final String PERSONNE_EXPORTER  = "2005";

    public static final String PROFIL_LIRE      = "2011";
    public static final String PROFIL_CREER     = "2012";
    public static final String PROFIL_MODIFIER  = "2013";
    public static final String PROFIL_SUPPRIMER = "2014";
    public static final String PROFIL_EXPORTER  = "2015";
    public static final String PROFIL_AFFECTER  = "2016";

    public static final String PERMISSION_LIRE      = "2021";
    public static final String PERMISSION_CREER     = "2022";
    public static final String PERMISSION_MODIFIER  = "2023";
    public static final String PERMISSION_SUPPRIMER = "2024";
    public static final String PERMISSION_EXPORTER  = "2025";

    public static final String BRANCHE_LIRE      = "2031";
    public static final String BRANCHE_CREER     = "2032";
    public static final String BRANCHE_MODIFIER  = "2033";
    public static final String BRANCHE_SUPPRIMER = "2034";
    public static final String BRANCHE_EXPORTER  = "2035";

    public static final String ROLE_LIRE       = "2041";
    public static final String ROLEPROFIL_LIRE = "2042";

    // --- Branche 3000 : Géographie & temps ---
    public static final String GEO_LIRE      = "3001";
    public static final String GEO_CREER     = "3002";
    public static final String GEO_MODIFIER  = "3003";
    public static final String GEO_SUPPRIMER = "3004";
    public static final String GEO_EXPORTER  = "3005";
    public static final String TEMPS_LIRE    = "3006";
    public static final String TEMPS_GERER   = "3007";

    // --- Branche 4000 : Catalogue ---
    public static final String CATALOGUE_LIRE      = "4001";
    public static final String CATALOGUE_CREER     = "4002";
    public static final String CATALOGUE_MODIFIER  = "4003";
    public static final String CATALOGUE_SUPPRIMER = "4004";
    public static final String CATALOGUE_EXPORTER  = "4005";

    // --- Branche 5000 : Stock & prestataires ---
    public static final String STOCK_LIRE      = "5001";
    public static final String STOCK_CREER     = "5002";
    public static final String STOCK_MODIFIER  = "5003";
    public static final String STOCK_SUPPRIMER = "5004";
    public static final String STOCK_EXPORTER  = "5005";

    // --- Branche 6000 : Modération des avis ---
    public static final String AVIS_LIRE      = "6001";
    public static final String AVIS_MODERER   = "6002";
    public static final String AVIS_REPONDRE  = "6003";
    public static final String AVIS_SUPPRIMER = "6004";
    public static final String AVIS_EXPORTER  = "6005";
    /** Répondre aux avis de ses propres services (rôle PRESTATAIRE). */
    public static final String AVIS_REPONDRE_SIEN = "6006";
    public static final String SIGNALEMENT_LIRE = "6007";
    public static final String SIGNALEMENT_GERER = "6008";

    // --- Branche 7000 : Commandes à distance ---
    public static final String COMMANDE_LIRE     = "7001";
    public static final String COMMANDE_TRAITER  = "7002";
    public static final String COMMANDE_ANNULER  = "7003";
    public static final String COMMANDE_EXPORTER = "7004";

    // --- Branche 8000 : Fournisseurs & clients ---
    public static final String FOURNISSEUR_LIRE  = "8001";
    public static final String FOURNISSEUR_GERER = "8002";
    public static final String CLIENT_LIRE       = "8003";
    public static final String CLIENT_GERER      = "8004";
    public static final String VALIDATION_LIRE   = "8005";
    public static final String VALIDATION_GERER  = "8006";
}
