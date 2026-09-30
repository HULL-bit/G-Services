package com.gservices.service.impl;

import com.gservices.dto.*;
import com.gservices.entity.*;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.HoraireMapper;
import com.gservices.mapper.InformationServiceMapper;
import com.gservices.repository.*;
import com.gservices.service.AvisService;
import com.gservices.service.VitrineService;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Vitrine publique : agrège l'offre active en objets prêts pour l'affichage.
 * Aucune règle de sécurité — ces méthodes alimentent des pages ouvertes à tous.
 */
@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class VitrineServiceImpl implements VitrineService {

    private final CategorieServiceRepository categorieRepo;
    private final ServiceRepository serviceRepo;
    private final CatalogueRepository catalogueRepo;
    private final ProduitRepository produitRepo;
    private final ArticleRepository articleRepo;
    private final InformationServiceRepository infoRepo;
    private final AvisRepository avisRepo;
    private final AvisService avisService;
    private final InformationServiceMapper infoMapper;
    private final HoraireMapper horaireMapper;

    public VitrineServiceImpl(CategorieServiceRepository categorieRepo, ServiceRepository serviceRepo,
                              CatalogueRepository catalogueRepo, ProduitRepository produitRepo,
                              ArticleRepository articleRepo, InformationServiceRepository infoRepo,
                              AvisRepository avisRepo, AvisService avisService,
                              InformationServiceMapper infoMapper, HoraireMapper horaireMapper) {
        this.categorieRepo = categorieRepo;
        this.serviceRepo = serviceRepo;
        this.catalogueRepo = catalogueRepo;
        this.produitRepo = produitRepo;
        this.articleRepo = articleRepo;
        this.infoRepo = infoRepo;
        this.avisRepo = avisRepo;
        this.avisService = avisService;
        this.infoMapper = infoMapper;
        this.horaireMapper = horaireMapper;
    }

    @Override
    public List<CategorieServiceDto> categories() {
        return categorieRepo.findByEtatTrueOrderByOrdreAffichageAscLibelleAsc().stream()
                .map(c -> {
                    CategorieServiceDto dto = new CategorieServiceDto();
                    dto.setId(c.getId());
                    dto.setLibelle(c.getLibelle());
                    dto.setDescription(c.getDescription());
                    dto.setIcone(c.getIcone());
                    dto.setOrdreAffichage(c.getOrdreAffichage());
                    dto.setNbServices(c.getServices().stream()
                            .filter(AbstractEntity::isEtat).filter(com.gservices.entity.Service::isValide)
                            .filter(sv -> !sv.isBloque()).count());
                    return dto;
                }).toList();
    }

    @Override
    public List<ServiceVitrineDto> servicesParCategorie(Long idCategorie) {
        return serviceRepo.findByCategorieServiceIdAndEtatTrueOrderByLibelleAsc(idCategorie).stream()
                .filter(com.gservices.entity.Service::isValide)
                .filter(s -> !s.isBloque())
                .map(this::carte).toList();
    }

    @Override
    public List<ServiceVitrineDto> servicesEnAvant(int limite) {
        return serviceRepo.findByEtatTrueOrderByLibelleAsc().stream()
                .filter(com.gservices.entity.Service::isValide)
                .filter(s -> !s.isBloque())
                .map(this::carte)
                .sorted((a, b) -> Double.compare(b.getNoteMoyenne(), a.getNoteMoyenne()))
                .limit(limite)
                .toList();
    }

    @Override
    public List<ServiceVitrineDto> servicesGeolocalises() {
        return serviceRepo.findGeolocalises().stream().map(this::carte).toList();
    }

    @Override
    public ServiceDetailDto serviceDetail(Long idService) {
        com.gservices.entity.Service service = serviceRepo.findVitrineById(idService)
                .filter(AbstractEntity::isEtat)
                .filter(com.gservices.entity.Service::isValide)
                .filter(s -> !s.isBloque())
                .orElseThrow(() -> new ResourceNotFoundException(com.gservices.entity.Service.class, idService));

        ServiceDetailDto detail = new ServiceDetailDto();
        detail.setService(carte(service));

        InformationService info = service.getInformationService();
        if (info != null && info.isEtat()) {
            detail.setPrestataire(infoMapper.toDto(info));
            detail.setHoraires(info.getHoraires().stream()
                    .filter(AbstractEntity::isEtat)
                    .sorted((x, y) -> Integer.compare(x.getJour().getNumeroJourSemaine(), y.getJour().getNumeroJourSemaine()))
                    .map(horaireMapper::toDto).toList());
        }

        for (Catalogue cat : catalogueRepo.findByServiceIdAndEtatTrueOrderByLibelleAsc(idService)) {
            CatalogueVitrineDto cv = new CatalogueVitrineDto();
            cv.setId(cat.getId());
            cv.setLibelle(cat.getLibelle());
            cv.setDescription(cat.getDescription());
            cv.setIcone(cat.getIcone());
            for (Produit prod : produitRepo.findByCatalogueIdAndEtatTrueOrderByLibelleAsc(cat.getId())) {
                ProduitVitrineDto pv = new ProduitVitrineDto();
                pv.setId(prod.getId());
                pv.setLibelle(prod.getLibelle());
                pv.setDescription(prod.getDescription());
                pv.setImage(prod.getImage());
                pv.setNoteMoyenne(avisRepo.moyenneProduit(prod.getId()));
                pv.setNombreAvis(avisRepo.findByProduitIdAndEstModereTrueAndEtatTrueOrderByDateDesc(prod.getId()).size());
                for (Article art : articleRepo.findByProduitIdAndEtatTrueOrderByReferenceAsc(prod.getId())) {
                    ProduitVitrineDto.ArticleVitrineDto av = new ProduitVitrineDto.ArticleVitrineDto();
                    av.setId(art.getId());
                    av.setReference(art.getReference());
                    av.setDescription(art.getDescription());
                    av.setPrix(art.getPrix());
                    av.setPrixNet(art.getPrixNet());
                    av.setDevise(art.getDevise());
                    av.setPromotion(art.isPromotion());
                    av.setTauxRemisePourcentage(art.getTauxRemisePourcentage());
                    av.setDisponibilite(art.isDisponibilite());
                    pv.getArticles().add(av);
                }
                cv.getProduits().add(pv);
            }
            detail.getCatalogues().add(cv);
        }

        detail.setSynthese(avisService.syntheseService(idService));
        detail.setAvis(avisService.avisPublicsService(idService));
        return detail;
    }

    // ---------------------------------------------------------------- util

    private ServiceVitrineDto carte(com.gservices.entity.Service s) {
        ServiceVitrineDto dto = new ServiceVitrineDto();
        dto.setId(s.getId());
        dto.setLibelle(s.getLibelle());
        dto.setDescription(s.getDescription());
        dto.setImage(s.getImage());
        dto.setPrixIndicatif(s.getPrixIndicatif());
        dto.setCommandeADistance(s.isCommandeADistance());
        if (s.getCategorieService() != null) {
            dto.setCategorieLibelle(s.getCategorieService().getLibelle());
            dto.setCategorieIcone(s.getCategorieService().getIcone());
        }
        InformationService info = s.getInformationService();
        dto.setDisponible(info != null && info.isEtat() && info.isDisponibilite());
        if (info != null) {
            dto.setAdresse(info.getAdresse());
            if (info.getPosition() != null) {
                dto.setLatitude(info.getPosition().getLatitude());
                dto.setLongitude(info.getPosition().getLongitude());
                if (info.getPosition().getVille() != null) {
                    dto.setVilleLibelle(info.getPosition().getVille().getLibelle());
                }
            }
        }
        dto.setNoteMoyenne(avisRepo.moyenneService(s.getId()));
        dto.setNombreAvis(avisRepo.findByServiceIdAndEstModereTrueAndEtatTrueOrderByDateDesc(s.getId()).size());
        dto.setNbCatalogues((int) catalogueRepo.countByServiceId(s.getId()));
        if (s.getProprietaire() != null) {
            dto.setProprietaireId(s.getProprietaire().getId());
        }
        return dto;
    }
}
