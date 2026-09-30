package com.gservices.repository;

import com.gservices.entity.Personne;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PersonneRepository
        extends JpaRepository<Personne, Long>, JpaSpecificationExecutor<Personne> {

    /** Chargement pour l'authentification : profils + permissions + surcharges en une requête. */
    @EntityGraph(attributePaths = {
            "roles", "roles.profil", "roles.profil.roleProfils", "roles.profil.roleProfils.permission",
            "permissionsDirectes", "permissionsDirectes.permission"
    })
    Optional<Personne> findByLoginIgnoreCase(String login);

    @EntityGraph(attributePaths = {
            "roles", "roles.profil", "roles.profil.roleProfils",
            "roles.profil.roleProfils.permission", "roles.profil.roleProfils.permission.branchePermission",
            "permissionsDirectes", "permissionsDirectes.permission"
    })
    Optional<Personne> findWithDroitsById(Long id);

    @EntityGraph(attributePaths = {"categorieSpecialite", "roles", "roles.profil"})
    Optional<Personne> findWithSpecialiteById(Long id);

    /**
     * Fournisseurs (profil {@code PRESTATAIRE}) actifs spécialisés dans une
     * catégorie donnée — seuls candidats possibles au rôle de propriétaire d'un
     * service de cette catégorie.
     */
    @org.springframework.data.jpa.repository.Query("""
            select distinct p from Personne p
              join p.roles r
              join r.profil pr
            where p.etat = true and r.etat = true
              and upper(pr.libelle) = 'PRESTATAIRE'
              and p.categorieSpecialite.id = :idCategorie
            order by p.nom, p.prenom
            """)
    java.util.List<Personne> findFournisseursParSpecialite(
            @org.springframework.data.repository.query.Param("idCategorie") Long idCategorie);

    /** Comptes en attente de validation par un administrateur. */
    @EntityGraph(attributePaths = {"roles", "roles.profil", "categorieSpecialite"})
    java.util.List<Personne> findByValideFalseAndEtatTrueOrderByDateInscriptionAsc();

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByEmail1IgnoreCase(String email1);

    long countByEtatTrue();

    long countByVerrouilleJusquaAfter(java.time.LocalDateTime instant);

    java.util.List<Personne> findByVerrouilleJusquaAfterOrderByVerrouilleJusquaDesc(java.time.LocalDateTime instant);
}
