package com.gservices.service;

import com.gservices.dto.FavoriDto;

import java.util.List;

/**
 * Espace favoris (classe d'association hors UML {@code Personne × Service}) —
 * auto-service, comme le dépôt d'avis : un client gère ses propres favoris.
 */
public interface FavoriService {

    /** {@code true} si le client connecté a ce service en favori. */
    boolean estFavori(Long idService);

    /** Nombre total de personnes ayant ce service en favori (public). */
    long nbFavoris(Long idService);

    /** Bascule favori/non-favori pour le client connecté ; renvoie le nouvel état. */
    boolean basculerFavori(Long idService);

    /** Les favoris actifs du client connecté, du plus récent au plus ancien. */
    List<FavoriDto> mesFavoris();
}
