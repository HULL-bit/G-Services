package com.gservices.service;

import com.gservices.dto.SanctionDto;
import com.gservices.dto.SignalementDto;
import com.gservices.entity.MotifSignalement;
import com.gservices.entity.StatutSignalement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * « Système de sanction / surveillance de la plateforme » (nouveau cahier §3) :
 * un client signale un service, un administrateur examine puis rejette ou
 * sanctionne (bloque) le service signalé.
 */
public interface SignalementService {

    // ---------------------------------------------------- public (authentifié)
    /** Dépose un signalement pour l'utilisateur connecté. */
    void signaler(Long idService, MotifSignalement motif, String description);

    // ---------------------------------------------------- back-office : file
    Page<SignalementDto> rechercher(String filtre, StatutSignalement statut, Pageable pageable);
    long nombreNouveaux();
    void rejeter(Long idSignalement);
    /** Rejette le signalement ET bloque le service ({@link com.gservices.entity.Sanction}). */
    void sanctionner(Long idSignalement, String motifSanction);

    // ---------------------------------------------------- back-office : sanctions actives
    Page<SanctionDto> servicesBloques(String filtre, Pageable pageable);
    long nombreServicesBloques();
    /** Lève la sanction : le service redevient visible sur le front public. */
    void lever(Long idSanction);
}
