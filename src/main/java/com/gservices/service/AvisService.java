package com.gservices.service;

import com.gservices.dto.AvisDto;
import com.gservices.dto.SyntheseAvisDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** {@code Avis} : dépôt public (authentifié), modération back-office, agrégats. */
public interface AvisService {

    enum EtatModeration { TOUS, EN_ATTENTE, MODERES }

    // ---------------------------------------------------- back-office
    Page<AvisDto> rechercher(String filtre, EtatModeration etat, Pageable pageable);
    long nombreEnAttente();
    void approuver(Long idAvis);
    void rejeter(Long idAvis);
    void repondre(Long idAvis, String reponse);
    void basculerEtat(Long idAvis);

    /**
     * Réponse d'un <strong>prestataire</strong> à un avis publié sur l'un de ses
     * propres services. Refusé si l'utilisateur connecté n'en est pas le propriétaire
     * (ou modérateur global).
     */
    void repondreProprietaire(Long idAvis, String reponse);
    /** {@code true} si l'utilisateur connecté est le propriétaire du service noté. */
    boolean estProprietaireDuService(Long idService);

    // ---------------------------------------------------- public
    List<AvisDto> avisPublicsService(Long idService);
    List<AvisDto> avisPublicsProduit(Long idProduit);
    SyntheseAvisDto syntheseService(Long idService);
    SyntheseAvisDto syntheseProduit(Long idProduit);

    /** Dépose un avis pour l'utilisateur connecté (une seule fois par cible). */
    AvisDto deposerAvisService(Long idService, int note, String commentaire);
    AvisDto deposerAvisProduit(Long idProduit, int note, String commentaire);

    /** L'avis déjà déposé par l'utilisateur connecté sur cette cible, ou {@code null}. */
    AvisDto monAvisService(Long idService);
    AvisDto monAvisProduit(Long idProduit);
}
