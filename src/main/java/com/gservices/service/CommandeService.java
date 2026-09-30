package com.gservices.service;

import com.gservices.dto.ArticleDto;
import com.gservices.dto.CommandeDto;
import com.gservices.entity.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Commandes à distance : tunnel public (client) + traitement back-office
 * (administrateur ou fournisseur propriétaire du service commandé).
 */
public interface CommandeService {

    // ------------------------------------------------ tunnel public (client)
    /** Articles disponibles d'un service acceptant la commande à distance. */
    List<ArticleDto> articlesCommandables(Long idService);

    /** Enregistre une commande pour le client connecté (statut {@code NOUVELLE}). */
    CommandeDto passerCommande(CommandeDto dto);

    /** Commandes du client connecté. */
    List<CommandeDto> mesCommandes();

    /** Détail d'une commande du client connecté. */
    CommandeDto maCommande(Long id);

    // ------------------------------------------------ back-office
    Page<CommandeDto> rechercherCommandes(String filtre, StatutCommande statut, Pageable pageable);

    CommandeDto commande(Long id);

    /** Fait avancer (ou annule) une commande ; contrôle de périmètre fournisseur. */
    CommandeDto changerStatut(Long id, StatutCommande cible);

    /** Nombre de commandes non traitées dans le périmètre courant (badge). */
    long nbCommandesATraiter();
}
