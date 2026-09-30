package com.gservices.service;

import com.gservices.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Référentiel géographique : hiérarchie
 * {@code Continent → ZoneGeographique → Pays → Region → Ville → Position}.
 * Un seul service cohérent pour tout le module (consommé par l'onglet Géographie).
 */
public interface GeographieService {

    // --------------------------------------------------------- Continent
    Page<ContinentDto> rechercherContinents(String filtre, Pageable pageable);
    List<ContinentDto> continentsActifs();
    ContinentDto continent(Long id);
    ContinentDto creerContinent(ContinentDto dto);
    ContinentDto modifierContinent(Long id, ContinentDto dto);
    void basculerContinent(Long id);

    // --------------------------------------------------------- ZoneGeographique
    Page<ZoneGeographiqueDto> rechercherZones(String filtre, Pageable pageable);
    List<ZoneGeographiqueDto> zonesParContinent(Long idContinent);
    List<ZoneGeographiqueDto> zonesActives();
    ZoneGeographiqueDto creerZone(ZoneGeographiqueDto dto);
    ZoneGeographiqueDto modifierZone(Long id, ZoneGeographiqueDto dto);
    void basculerZone(Long id);

    // --------------------------------------------------------- Pays
    Page<PaysDto> rechercherPays(String filtre, Pageable pageable);
    List<PaysDto> paysParZone(Long idZone);
    List<PaysDto> paysActifs();
    List<PaysDto> paysParContinent(Long idContinent);
    PaysDto creerPays(PaysDto dto);
    PaysDto modifierPays(Long id, PaysDto dto);
    void basculerPays(Long id);

    // --------------------------------------------------------- Region
    Page<RegionDto> rechercherRegions(String filtre, Pageable pageable);
    List<RegionDto> regionsParPays(Long idPays);
    List<RegionDto> regionsActives();
    RegionDto creerRegion(RegionDto dto);
    RegionDto modifierRegion(Long id, RegionDto dto);
    void basculerRegion(Long id);

    // --------------------------------------------------------- Ville
    Page<VilleDto> rechercherVilles(String filtre, Pageable pageable);
    List<VilleDto> villesParRegion(Long idRegion);
    VilleDto ville(Long id);
    VilleDto creerVille(VilleDto dto);
    VilleDto modifierVille(Long id, VilleDto dto);
    void basculerVille(Long id);

    // --------------------------------------------------------- Position
    List<PositionDto> positionsParVille(Long idVille);
    PositionDto enregistrerPosition(Long idVille, PositionDto dto);
    void basculerPosition(Long idPosition);
    /** Villes actives possédant au moins une position — pour la carte. */
    List<VilleDto> villesGeolocalisees();
}
