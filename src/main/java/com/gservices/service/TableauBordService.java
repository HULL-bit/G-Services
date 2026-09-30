package com.gservices.service;

import com.gservices.dto.PersonneDto;
import com.gservices.dto.RepartitionDto;
import com.gservices.dto.TableauBordDto;

import java.util.List;

public interface TableauBordService {

    TableauBordDto chiffres();

    /** Nombre de permissions actives par branche (pour la mini-répartition). */
    List<RepartitionDto> repartitionPermissions();

    /** Comptes actuellement verrouillés (échecs de connexion), les plus récents d'abord. */
    List<PersonneDto> comptesVerrouilles();
}
