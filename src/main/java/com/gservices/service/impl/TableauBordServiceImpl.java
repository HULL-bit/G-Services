package com.gservices.service.impl;

import com.gservices.dto.PersonneDto;
import com.gservices.dto.RepartitionDto;
import com.gservices.dto.TableauBordDto;
import com.gservices.entity.CategorieService;
import com.gservices.entity.Permission;
import com.gservices.entity.StatutCommande;
import com.gservices.mapper.PersonneMapper;
import com.gservices.repository.*;
import com.gservices.service.TableauBordService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TableauBordServiceImpl implements TableauBordService {

    private final PersonneRepository personneRepository;
    private final ProfilRepository profilRepository;
    private final PermissionRepository permissionRepository;
    private final BranchePermissionRepository brancheRepository;
    private final PersonneMapper personneMapper;
    private final ServiceRepository serviceRepository;
    private final CategorieServiceRepository categorieServiceRepository;
    private final CommandeRepository commandeRepository;
    private final AvisRepository avisRepository;
    private final SignalementRepository signalementRepository;
    private final FavoriRepository favoriRepository;

    public TableauBordServiceImpl(PersonneRepository personneRepository,
                                  ProfilRepository profilRepository,
                                  PermissionRepository permissionRepository,
                                  BranchePermissionRepository brancheRepository,
                                  PersonneMapper personneMapper,
                                  ServiceRepository serviceRepository,
                                  CategorieServiceRepository categorieServiceRepository,
                                  CommandeRepository commandeRepository,
                                  AvisRepository avisRepository,
                                  SignalementRepository signalementRepository,
                                  FavoriRepository favoriRepository) {
        this.personneRepository = personneRepository;
        this.profilRepository = profilRepository;
        this.permissionRepository = permissionRepository;
        this.brancheRepository = brancheRepository;
        this.personneMapper = personneMapper;
        this.serviceRepository = serviceRepository;
        this.categorieServiceRepository = categorieServiceRepository;
        this.commandeRepository = commandeRepository;
        this.avisRepository = avisRepository;
        this.signalementRepository = signalementRepository;
        this.favoriRepository = favoriRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).DASHBOARD_LIRE)")
    public TableauBordDto chiffres() {
        TableauBordDto d = new TableauBordDto();

        d.setPersonnes(personneRepository.count());
        d.setPersonnesActives(personneRepository.countByEtatTrue());
        d.setPersonnesVerrouillees(personneRepository.countByVerrouilleJusquaAfter(LocalDateTime.now()));
        d.setPersonnesEnAttente(personneRepository.countByValideFalseAndEtatTrue());
        d.setProfils(profilRepository.count());
        d.setPermissions(permissionRepository.count());
        d.setBranches(brancheRepository.count());

        d.setServices(serviceRepository.countByEtatTrueAndValideTrue());
        d.setServicesGeolocalises(serviceRepository.findGeolocalises().size());
        d.setServicesEnAttente(serviceRepository.countByValideFalseAndEtatTrue());
        d.setServicesBloques(serviceRepository.countByBloqueTrueAndEtatTrue());
        d.setFournisseurs(personneRepository.countByProfilLibelle("PRESTATAIRE"));
        d.setClients(personneRepository.countByProfilLibelle("CLIENT"));
        d.setFavoris(favoriRepository.countByEtatTrue());

        d.setCommandes(commandeRepository.countByEtatTrue());
        d.setCommandesATraiter(commandeRepository.countByStatutNotInAndEtatTrue(
                List.of(StatutCommande.LIVREE, StatutCommande.ANNULEE)));
        d.setMontantCommandes(commandeRepository.montantTotal());

        d.setAvisPublies(avisRepository.countByEstModereTrueAndEtatTrue());
        d.setAvisEnAttente(avisRepository.countByEstModereFalseAndEtatTrue());
        d.setSignalementsNouveaux(signalementRepository.countByStatutAndEtatTrue(
                com.gservices.entity.StatutSignalement.NOUVEAU));

        return d;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).DASHBOARD_LIRE)")
    public List<RepartitionDto> repartitionPermissions() {
        List<Object[]> lignes = new ArrayList<>();
        brancheRepository.findByEtatTrueOrderByNiveauAsc().forEach(b -> {
            long nb = b.getPermissions().stream().filter(Permission::isEtat).count();
            if (nb > 0) {
                lignes.add(new Object[]{b.getLibelle(), nb});
            }
        });
        return versPourcentages(lignes);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).DASHBOARD_LIRE)")
    public List<RepartitionDto> repartitionServices() {
        List<Object[]> lignes = new ArrayList<>();
        for (CategorieService c : categorieServiceRepository.findByEtatTrueOrderByOrdreAffichageAscLibelleAsc()) {
            long nb = serviceRepository.countByCategorieServiceId(c.getId());
            if (nb > 0) {
                lignes.add(new Object[]{c.getLibelle(), nb});
            }
        }
        return versPourcentages(lignes);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).DASHBOARD_LIRE)")
    public List<RepartitionDto> repartitionCommandes() {
        List<Object[]> lignes = new ArrayList<>();
        for (StatutCommande s : StatutCommande.values()) {
            long nb = commandeRepository.countByStatutAndEtatTrue(s);
            if (nb > 0) {
                lignes.add(new Object[]{s.name(), nb});
            }
        }
        return versPourcentages(lignes);
    }

    private List<RepartitionDto> versPourcentages(List<Object[]> lignes) {
        long max = lignes.stream().mapToLong(l -> (long) l[1]).max().orElse(1);
        List<RepartitionDto> res = new ArrayList<>();
        for (Object[] l : lignes) {
            long v = (long) l[1];
            res.add(new RepartitionDto((String) l[0], v, (int) Math.round(v * 100.0 / max)));
        }
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).DASHBOARD_LIRE)")
    public List<PersonneDto> comptesVerrouilles() {
        return personneRepository
                .findByVerrouilleJusquaAfterOrderByVerrouilleJusquaDesc(LocalDateTime.now())
                .stream().map(personneMapper::toDto).toList();
    }
}
