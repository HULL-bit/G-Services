package com.gservices.service;

import com.gservices.dto.*;

import java.util.List;

/**
 * Lecture <strong>publique</strong> (sans authentification) de l'offre active :
 * catégories, services (prestataires), catalogues, produits, articles. Seuls les
 * éléments {@code etat = true} sont exposés.
 */
public interface VitrineService {

    List<CategorieServiceDto> categories();

    /** Services actifs d'une catégorie, avec note moyenne et nombre d'avis. */
    List<ServiceVitrineDto> servicesParCategorie(Long idCategorie);

    /** Quelques services mis en avant (toutes catégories) pour la page d'accueil. */
    List<ServiceVitrineDto> servicesEnAvant(int limite);

    /** Services actifs géolocalisés (avec une Position) — pour la carte publique. */
    List<ServiceVitrineDto> servicesGeolocalises();

    /** Fiche complète d'un service : prestataire, horaires, catalogues + produits + articles. */
    ServiceDetailDto serviceDetail(Long idService);
}
