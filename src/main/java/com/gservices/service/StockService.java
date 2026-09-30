package com.gservices.service;

import com.gservices.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Module Stock &amp; prestataires :
 * <ul>
 *   <li>{@code Article ─ Stock ─ Lot ─ Mois} : disponibilité, alertes de seuil,
 *       exclusion des lots périmés ;</li>
 *   <li>{@code Service ─ InformationService ─ Position} et
 *       {@code InformationService ─ Horaire ─ Jour} : fiches prestataires.</li>
 * </ul>
 */
public interface StockService {

    // ----------------------------------------------------------- Stock
    Page<StockDto> rechercherStocks(String filtre, Long idFournisseur, Pageable pageable);

    /** Fournisseurs (propriétaires de service) ayant au moins un stock — filtre de l'écran admin. */
    List<PersonneDto> fournisseursAvecStock();
    StockDto stock(Long id);
    /** Ouvre un stock pour un article qui n'en a pas encore. */
    StockDto ouvrirStock(StockDto dto);
    StockDto modifierStock(Long id, StockDto dto);
    void basculerStock(Long id);
    /** Articles actifs sans stock (pour le formulaire d'ouverture). */
    List<ArticleDto> articlesSansStock();

    // ----------------------------------------------------------- Lot
    List<LotDto> lotsParStock(Long idStock);
    LotDto enregistrerLot(Long idStock, LotDto dto);
    void basculerLot(Long idLot);

    // Référentiel temps pour la cascade Année → Mois du formulaire Lot
    List<AnneeDto> anneesActives();
    List<MoisDto> moisParAnnee(Long idAnnee);

    // ----------------------------------------------------------- InformationService
    Page<InformationServiceDto> rechercherPrestataires(String filtre, Pageable pageable);
    InformationServiceDto prestataire(Long id);
    InformationServiceDto creerPrestataire(InformationServiceDto dto);
    InformationServiceDto modifierPrestataire(Long id, InformationServiceDto dto);
    void basculerPrestataire(Long id);
    /** Services actifs sans fiche prestataire. */
    List<ServiceDto> servicesSansFiche();
    /** Positions actives (pour rattacher une fiche à un point GPS). */
    List<PositionDto> positionsActives();

    // ----------------------------------------------------------- Horaire
    List<HoraireDto> horairesParPrestataire(Long idPrestataire);
    HoraireDto enregistrerHoraire(Long idPrestataire, HoraireDto dto);
    void basculerHoraire(Long idHoraire);
    List<JourDto> jours();
}
