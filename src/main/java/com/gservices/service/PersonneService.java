package com.gservices.service;

import com.gservices.dto.PersonneDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonneService {

    /** Filtre d'état pour l'écran Personnes (rien à voir avec {@code Personne.etat} seul : inclut le verrouillage). */
    enum EtatFiltre { TOUS, ACTIFS, INACTIFS, VERROUILLES }

    /**
     * @param idProfil restreint aux personnes ayant ce profil (rôle actif) ; {@code null} = tous.
     * @param etat restreint par état de compte ; {@code null} ou {@code TOUS} = tous.
     */
    Page<PersonneDto> rechercher(String filtre, Long idProfil, EtatFiltre etat, Pageable pageable);

    List<PersonneDto> toutesActives();

    /**
     * Catégories de services actives — référentiel pour choisir la spécialité
     * d'un fournisseur dans le formulaire Personne (n'exige pas les droits du
     * module Catalogue).
     */
    List<com.gservices.dto.CategorieServiceDto> categoriesReferentiel();

    PersonneDto parId(Long id);

    /** Création : {@code nouveauMotDePasse} obligatoire, encodé BCrypt. */
    PersonneDto creer(PersonneDto dto);

    /** MAJ de l'état civil / contact (ni login ni mot de passe). */
    PersonneDto modifier(Long id, PersonneDto dto);

    void basculerEtat(Long id);

    /** Réinitialisation du mot de passe par un administrateur. */
    void reinitialiserMotDePasse(Long id, String nouveauMotDePasse);

    /** Changement de son propre mot de passe (contrôle de l'ancien). */
    void changerMonMotDePasse(String ancien, String nouveau);

    /** Lève le verrou « trop d'échecs de connexion ». */
    void deverrouiller(Long id);

    /**
     * Toutes les permissions actives, chacune indiquant si elle est effectivement
     * accordée à la personne et pourquoi (héritée d'un profil, ajoutée ou retirée
     * explicitement). Sert d'état initial à l'écran « Permissions effectives ».
     */
    java.util.List<com.gservices.dto.PermissionEffectiveDto> permissionsEffectives(Long idPersonne);

    /**
     * Réconcilie les surcharges {@code PersonnePermission} de la personne à partir
     * de l'ensemble des identifiants de permission cochés dans l'écran : ajoute une
     * surcharge « accordée » si coché sans profil porteur, « retirée » si décoché
     * alors qu'un profil la porte, et supprime la surcharge si elle redevient inutile.
     */
    void majPermissions(Long idPersonne, java.util.Set<Long> permissionIdsCochees);

    /** Auto-inscription publique d'un visiteur : crée la personne + lui assigne le profil {@code CLIENT}. */
    void inscrire(com.gservices.dto.InscriptionDto dto);
}
