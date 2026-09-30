package com.gservices.entity;

/**
 * Cycle de vie d'une {@link Commande} passée à distance.
 *
 * <p>{@code NOUVELLE → CONFIRMEE → EN_PREPARATION → EXPEDIEE → LIVREE}, avec
 * {@code ANNULEE} possible tant que la commande n'est pas livrée.</p>
 */
public enum StatutCommande {
    NOUVELLE,
    CONFIRMEE,
    EN_PREPARATION,
    EXPEDIEE,
    LIVREE,
    ANNULEE;

    /** {@code true} si la commande peut encore changer d'état (non terminale). */
    public boolean estOuverte() {
        return this != LIVREE && this != ANNULEE;
    }
}
