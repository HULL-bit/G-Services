package com.gservices.service;

import com.gservices.dto.CategorieServiceDto;
import com.gservices.dto.CreationCompteDto;
import com.gservices.dto.PersonneDto;
import com.gservices.dto.ServiceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Administration des <strong>fournisseurs</strong> et des <strong>clients</strong> :
 * écrans dédiés du back-office, création de comptes, et file de validation des
 * comptes auto-inscrits et des services créés par les fournisseurs.
 */
public interface GestionComptesService {

    // -------------------------------------------------- Fournisseurs
    Page<PersonneDto> rechercherFournisseurs(String filtre, Pageable pageable);
    PersonneDto creerFournisseur(CreationCompteDto dto);
    void basculerFournisseur(Long idPersonne);

    // -------------------------------------------------- Clients
    Page<PersonneDto> rechercherClients(String filtre, Pageable pageable);
    PersonneDto creerClient(CreationCompteDto dto);
    void basculerClient(Long idPersonne);

    // -------------------------------------------------- Validation
    List<PersonneDto> comptesEnAttente();
    List<ServiceDto> servicesEnAttente();
    void validerCompte(Long idPersonne);
    void refuserCompte(Long idPersonne);
    void validerService(Long idService);
    void refuserService(Long idService);

    // -------------------------------------------------- Référentiel
    List<CategorieServiceDto> categoriesReferentiel();
}
