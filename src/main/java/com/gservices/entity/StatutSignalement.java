package com.gservices.entity;

/** Cycle de vie d'un {@link Signalement} : cf. §3 « surveillance de la plateforme ». */
public enum StatutSignalement {
    /** Déposé, pas encore examiné par un administrateur. */
    NOUVEAU,
    /** Examiné, jugé infondé — aucune sanction. */
    REJETE,
    /** Examiné, a donné lieu à une {@link Sanction} (service bloqué). */
    TRAITE
}
