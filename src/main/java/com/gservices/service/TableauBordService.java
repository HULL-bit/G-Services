package com.gservices.service;

import com.gservices.dto.PersonneDto;
import com.gservices.dto.RepartitionDto;
import com.gservices.dto.TableauBordDto;

import java.util.List;

public interface TableauBordService {

    TableauBordDto chiffres();

    /** Nombre de permissions actives par branche (pour la mini-répartition sécurité). */
    List<RepartitionDto> repartitionPermissions();

    /** Services actifs par catégorie (pour le donut « Offre par catégorie »). */
    List<RepartitionDto> repartitionServices();

    /** Commandes par statut, code brut en libellé (ex. {@code NOUVELLE}) — à localiser côté vue. */
    List<RepartitionDto> repartitionCommandes();

    /** Comptes actuellement verrouillés (échecs de connexion), les plus récents d'abord. */
    List<PersonneDto> comptesVerrouilles();
}
