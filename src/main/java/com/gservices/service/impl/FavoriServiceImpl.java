package com.gservices.service.impl;

import com.gservices.dto.FavoriDto;
import com.gservices.entity.Favori;
import com.gservices.entity.Personne;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.repository.AvisRepository;
import com.gservices.repository.FavoriRepository;
import com.gservices.repository.PersonneRepository;
import com.gservices.repository.ServiceRepository;
import com.gservices.security.SecurityUtils;
import com.gservices.service.FavoriService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@Transactional
public class FavoriServiceImpl implements FavoriService {

    private final FavoriRepository favoriRepo;
    private final ServiceRepository serviceRepo;
    private final PersonneRepository personneRepo;
    private final AvisRepository avisRepo;

    public FavoriServiceImpl(FavoriRepository favoriRepo, ServiceRepository serviceRepo,
                             PersonneRepository personneRepo, AvisRepository avisRepo) {
        this.favoriRepo = favoriRepo;
        this.serviceRepo = serviceRepo;
        this.personneRepo = personneRepo;
        this.avisRepo = avisRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estFavori(Long idService) {
        return SecurityUtils.currentPersonneId()
                .map(id -> favoriRepo.existsByPersonneIdAndServiceIdAndEtatTrue(id, idService))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long nbFavoris(Long idService) {
        return favoriRepo.countByServiceIdAndEtatTrue(idService);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public boolean basculerFavori(Long idService) {
        Long idPersonne = SecurityUtils.currentPersonneId()
                .orElseThrow(() -> new BusinessException("Connectez-vous pour gérer vos favoris."));
        com.gservices.entity.Service service = serviceRepo.findById(idService)
                .orElseThrow(() -> new ResourceNotFoundException(com.gservices.entity.Service.class, idService));

        Favori favori = favoriRepo.findByPersonneIdAndServiceId(idPersonne, idService).orElse(null);
        if (favori == null) {
            favori = new Favori();
            favori.setPersonne(personneRepo.getReferenceById(idPersonne));
            favori.setService(service);
            favori.setDateAjout(LocalDateTime.now());
            favori.setEtat(true);
            favoriRepo.save(favori);
            return true;
        }
        boolean nouvelEtat = !favori.isEtat();
        favori.setEtat(nouvelEtat);
        if (nouvelEtat) {
            favori.setDateAjout(LocalDateTime.now());
        }
        return nouvelEtat;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<FavoriDto> mesFavoris() {
        Long idPersonne = SecurityUtils.currentPersonneId()
                .orElseThrow(() -> new BusinessException("Connectez-vous pour voir vos favoris."));
        return favoriRepo.findByPersonneIdAndEtatTrueOrderByDateAjoutDesc(idPersonne).stream()
                .filter(f -> f.getService() != null && f.getService().isEtat() && f.getService().isValide())
                .map(this::versDto)
                .toList();
    }

    private FavoriDto versDto(Favori f) {
        com.gservices.entity.Service s = f.getService();
        FavoriDto dto = new FavoriDto();
        dto.setId(f.getId());
        dto.setDateAjout(f.getDateAjout());
        dto.setServiceId(s.getId());
        dto.setServiceLibelle(s.getLibelle());
        dto.setServiceDescription(s.getDescription());
        if (s.getCategorieService() != null) {
            dto.setCategorieLibelle(s.getCategorieService().getLibelle());
            dto.setCategorieIcone(s.getCategorieService().getIcone());
        }
        var info = s.getInformationService();
        dto.setDisponible(info != null && info.isEtat() && info.isDisponibilite());
        if (info != null && info.getPosition() != null && info.getPosition().getVille() != null) {
            dto.setVilleLibelle(info.getPosition().getVille().getLibelle());
        }
        dto.setNoteMoyenne(avisRepo.moyenneService(s.getId()));
        dto.setNombreAvis(avisRepo.findByServiceIdAndEstModereTrueAndEtatTrueOrderByDateDesc(s.getId()).size());
        return dto;
    }
}
