package com.gservices.service.impl;

import com.gservices.dto.*;
import com.gservices.entity.*;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.*;
import com.gservices.repository.*;
import com.gservices.security.Perms;
import com.gservices.service.GeographieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implémentation du référentiel géographique. Chaque niveau de la hiérarchie
 * résout et rattache son parent avant persistance ; la désactivation est un
 * <em>soft-delete</em> ({@code etat = false}) et n'est possible que si le niveau
 * inférieur ne contient plus d'élément actif.
 */
@Service
@Transactional
public class GeographieServiceImpl implements GeographieService {

    private final ContinentRepository continentRepo;
    private final ZoneGeographiqueRepository zoneRepo;
    private final PaysRepository paysRepo;
    private final RegionRepository regionRepo;
    private final VilleRepository villeRepo;
    private final PositionRepository positionRepo;
    private final ContinentMapper continentMapper;
    private final ZoneGeographiqueMapper zoneMapper;
    private final PaysMapper paysMapper;
    private final RegionMapper regionMapper;
    private final VilleMapper villeMapper;
    private final PositionMapper positionMapper;

    public GeographieServiceImpl(ContinentRepository continentRepo, ZoneGeographiqueRepository zoneRepo,
                                 PaysRepository paysRepo, RegionRepository regionRepo,
                                 VilleRepository villeRepo, PositionRepository positionRepo,
                                 ContinentMapper continentMapper, ZoneGeographiqueMapper zoneMapper,
                                 PaysMapper paysMapper, RegionMapper regionMapper,
                                 VilleMapper villeMapper, PositionMapper positionMapper) {
        this.continentRepo = continentRepo;
        this.zoneRepo = zoneRepo;
        this.paysRepo = paysRepo;
        this.regionRepo = regionRepo;
        this.villeRepo = villeRepo;
        this.positionRepo = positionRepo;
        this.continentMapper = continentMapper;
        this.zoneMapper = zoneMapper;
        this.paysMapper = paysMapper;
        this.regionMapper = regionMapper;
        this.villeMapper = villeMapper;
        this.positionMapper = positionMapper;
    }

    // ========================================================== Continent =====

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public Page<ContinentDto> rechercherContinents(String filtre, Pageable pageable) {
        return continentRepo.findAll(texteSpec(filtre, "libelle", "code"), pageable).map(continentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<ContinentDto> continentsActifs() {
        return continentRepo.findByEtatTrueOrderByLibelleAsc().stream().map(continentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public ContinentDto continent(Long id) {
        return continentMapper.toDto(chargerContinent(id));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_CREER)")
    public ContinentDto creerContinent(ContinentDto dto) {
        if (StringUtils.hasText(dto.getCode()) && continentRepo.existsByCodeIgnoreCase(dto.getCode())) {
            throw new BusinessException("Le code de continent « " + dto.getCode() + " » existe déjà.");
        }
        return continentMapper.toDto(continentRepo.save(continentMapper.toEntity(dto)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_MODIFIER)")
    public ContinentDto modifierContinent(Long id, ContinentDto dto) {
        Continent c = chargerContinent(id);
        if (StringUtils.hasText(dto.getCode())) {
            continentRepo.findByCodeIgnoreCase(dto.getCode())
                    .filter(autre -> !autre.getId().equals(id))
                    .ifPresent(autre -> {
                        throw new BusinessException("Le code « " + dto.getCode() + " » est déjà utilisé.");
                    });
        }
        continentMapper.update(c, dto);
        return continentMapper.toDto(c);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_SUPPRIMER)")
    public void basculerContinent(Long id) {
        Continent c = chargerContinent(id);
        if (c.isEtat() && zoneRepo.countByContinentId(id) > 0) {
            throw new BusinessException("Ce continent contient des zones : désactivez-les d'abord.");
        }
        c.setEtat(!c.isEtat());
    }

    // ========================================================== Zone ==========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public Page<ZoneGeographiqueDto> rechercherZones(String filtre, Pageable pageable) {
        return zoneRepo.findAll(texteSpec(filtre, "libelle", "code"), pageable).map(zoneMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<ZoneGeographiqueDto> zonesParContinent(Long idContinent) {
        if (idContinent == null) {
            return List.of();
        }
        return zoneRepo.findByContinentIdAndEtatTrueOrderByLibelleAsc(idContinent)
                .stream().map(zoneMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<ZoneGeographiqueDto> zonesActives() {
        return zoneRepo.findByEtatTrueOrderByLibelleAsc().stream().map(zoneMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_CREER)")
    public ZoneGeographiqueDto creerZone(ZoneGeographiqueDto dto) {
        if (StringUtils.hasText(dto.getCode()) && zoneRepo.existsByCodeIgnoreCase(dto.getCode())) {
            throw new BusinessException("Le code de zone « " + dto.getCode() + " » existe déjà.");
        }
        ZoneGeographique z = zoneMapper.toEntity(dto);
        z.setContinent(chargerContinent(dto.getContinentId()));
        return zoneMapper.toDto(zoneRepo.save(z));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_MODIFIER)")
    public ZoneGeographiqueDto modifierZone(Long id, ZoneGeographiqueDto dto) {
        ZoneGeographique z = zoneRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ZoneGeographique.class, id));
        if (StringUtils.hasText(dto.getCode())) {
            zoneRepo.findByCodeIgnoreCase(dto.getCode())
                    .filter(autre -> !autre.getId().equals(id))
                    .ifPresent(autre -> {
                        throw new BusinessException("Le code « " + dto.getCode() + " » est déjà utilisé.");
                    });
        }
        zoneMapper.update(z, dto);
        z.setContinent(chargerContinent(dto.getContinentId()));
        return zoneMapper.toDto(z);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_SUPPRIMER)")
    public void basculerZone(Long id) {
        ZoneGeographique z = zoneRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ZoneGeographique.class, id));
        if (z.isEtat() && paysRepo.countByZoneGeographiqueId(id) > 0) {
            throw new BusinessException("Cette zone contient des pays : désactivez-les d'abord.");
        }
        z.setEtat(!z.isEtat());
    }

    // ========================================================== Pays ==========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public Page<PaysDto> rechercherPays(String filtre, Pageable pageable) {
        return paysRepo.findAll(texteSpec(filtre, "libelle", "codeIso2", "codeIso3", "capitale"), pageable)
                .map(paysMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<PaysDto> paysParZone(Long idZone) {
        if (idZone == null) {
            return List.of();
        }
        return paysRepo.findByZoneGeographiqueIdAndEtatTrueOrderByLibelleAsc(idZone)
                .stream().map(paysMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<PaysDto> paysActifs() {
        return paysRepo.findByEtatTrueOrderByLibelleAsc().stream().map(paysMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<PaysDto> paysParContinent(Long idContinent) {
        if (idContinent == null) {
            return List.of();
        }
        return paysRepo.findActifsParContinent(idContinent).stream().map(paysMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_CREER)")
    public PaysDto creerPays(PaysDto dto) {
        controlerIso(dto, null);
        Pays p = paysMapper.toEntity(dto);
        p.setZoneGeographique(chargerZone(dto.getZoneGeographiqueId()));
        return paysMapper.toDto(paysRepo.save(p));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_MODIFIER)")
    public PaysDto modifierPays(Long id, PaysDto dto) {
        Pays p = paysRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Pays.class, id));
        controlerIso(dto, id);
        paysMapper.update(p, dto);
        p.setZoneGeographique(chargerZone(dto.getZoneGeographiqueId()));
        return paysMapper.toDto(p);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_SUPPRIMER)")
    public void basculerPays(Long id) {
        Pays p = paysRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Pays.class, id));
        if (p.isEtat() && regionRepo.countByPaysId(id) > 0) {
            throw new BusinessException("Ce pays contient des régions : désactivez-les d'abord.");
        }
        p.setEtat(!p.isEtat());
    }

    // ========================================================== Region ========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public Page<RegionDto> rechercherRegions(String filtre, Pageable pageable) {
        return regionRepo.findAll(texteSpec(filtre, "libelle", "codeRegion", "chefLieu"), pageable)
                .map(regionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<RegionDto> regionsParPays(Long idPays) {
        if (idPays == null) {
            return List.of();
        }
        return regionRepo.findByPaysIdAndEtatTrueOrderByLibelleAsc(idPays)
                .stream().map(regionMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<RegionDto> regionsActives() {
        return regionRepo.findByEtatTrueOrderByLibelleAsc().stream().map(regionMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_CREER)")
    public RegionDto creerRegion(RegionDto dto) {
        Region r = regionMapper.toEntity(dto);
        r.setPays(chargerPays(dto.getPaysId()));
        return regionMapper.toDto(regionRepo.save(r));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_MODIFIER)")
    public RegionDto modifierRegion(Long id, RegionDto dto) {
        Region r = regionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Region.class, id));
        regionMapper.update(r, dto);
        r.setPays(chargerPays(dto.getPaysId()));
        return regionMapper.toDto(r);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_SUPPRIMER)")
    public void basculerRegion(Long id) {
        Region r = regionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Region.class, id));
        if (r.isEtat() && villeRepo.countByRegionId(id) > 0) {
            throw new BusinessException("Cette région contient des villes : désactivez-les d'abord.");
        }
        r.setEtat(!r.isEtat());
    }

    // ========================================================== Ville =========

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public Page<VilleDto> rechercherVilles(String filtre, Pageable pageable) {
        return villeRepo.findAll(texteSpec(filtre, "libelle", "codePostal"), pageable).map(villeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<VilleDto> villesParRegion(Long idRegion) {
        if (idRegion == null) {
            return List.of();
        }
        return villeRepo.findByRegionIdAndEtatTrueOrderByLibelleAsc(idRegion)
                .stream().map(villeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public VilleDto ville(Long id) {
        return villeMapper.toDto(villeRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Ville.class, id)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_CREER)")
    public VilleDto creerVille(VilleDto dto) {
        Ville v = villeMapper.toEntity(dto);
        v.setRegion(chargerRegion(dto.getRegionId()));
        return villeMapper.toDto(villeRepo.save(v));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_MODIFIER)")
    public VilleDto modifierVille(Long id, VilleDto dto) {
        Ville v = villeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Ville.class, id));
        villeMapper.update(v, dto);
        v.setRegion(chargerRegion(dto.getRegionId()));
        return villeMapper.toDto(v);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_SUPPRIMER)")
    public void basculerVille(Long id) {
        Ville v = villeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Ville.class, id));
        v.setEtat(!v.isEtat());
    }

    // ========================================================== Position ======

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<PositionDto> positionsParVille(Long idVille) {
        return positionRepo.findByVilleId(idVille).stream().map(positionMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_MODIFIER)")
    public PositionDto enregistrerPosition(Long idVille, PositionDto dto) {
        Ville ville = villeRepo.findById(idVille)
                .orElseThrow(() -> new ResourceNotFoundException(Ville.class, idVille));
        controlerCoordonnees(dto);
        Position position;
        if (dto.getId() != null) {
            position = positionRepo.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(Position.class, dto.getId()));
            positionMapper.update(position, dto);
        } else {
            position = positionMapper.toEntity(dto);
            position.setVille(ville);
        }
        return positionMapper.toDto(positionRepo.save(position));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_SUPPRIMER)")
    public void basculerPosition(Long idPosition) {
        Position p = positionRepo.findById(idPosition)
                .orElseThrow(() -> new ResourceNotFoundException(Position.class, idPosition));
        p.setEtat(!p.isEtat());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).GEO_LIRE)")
    public List<VilleDto> villesGeolocalisees() {
        return villeRepo.findByEtatTrueAndPositionsIsNotEmpty().stream()
                .map(villeMapper::toDto).toList();
    }

    // ========================================================== util ==========

    private Continent chargerContinent(Long id) {
        return continentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Continent.class, id));
    }

    private ZoneGeographique chargerZone(Long id) {
        return zoneRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(ZoneGeographique.class, id));
    }

    private Pays chargerPays(Long id) {
        return paysRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Pays.class, id));
    }

    private Region chargerRegion(Long id) {
        return regionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Region.class, id));
    }

    private void controlerIso(PaysDto dto, Long idCourant) {
        if (StringUtils.hasText(dto.getCodeIso2())) {
            paysRepo.findByCodeIso2IgnoreCase(dto.getCodeIso2())
                    .filter(autre -> !autre.getId().equals(idCourant))
                    .ifPresent(a -> {
                        throw new BusinessException("Le code ISO2 « " + dto.getCodeIso2() + " » est déjà utilisé.");
                    });
        }
    }

    private void controlerCoordonnees(PositionDto dto) {
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new BusinessException("La latitude et la longitude sont obligatoires.");
        }
        if (dto.getLatitude() < -90 || dto.getLatitude() > 90) {
            throw new BusinessException("La latitude doit être comprise entre -90 et 90.");
        }
        if (dto.getLongitude() < -180 || dto.getLongitude() > 180) {
            throw new BusinessException("La longitude doit être comprise entre -180 et 180.");
        }
    }

    /** Filtre {@code LIKE} insensible à la casse sur plusieurs colonnes texte. */
    private static <T> Specification<T> texteSpec(String filtre, String... champs) {
        if (!StringUtils.hasText(filtre)) {
            return null;
        }
        String motif = "%" + filtre.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            var predicats = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            for (String champ : champs) {
                predicats.add(cb.like(cb.lower(root.get(champ)), motif));
            }
            return cb.or(predicats.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
