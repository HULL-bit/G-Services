package com.gservices.repository;

import com.gservices.entity.Service;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository
        extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {
    List<Service> findByEtatTrueOrderByLibelleAsc();
    List<Service> findByInformationServiceIsNullAndEtatTrueOrderByLibelleAsc();
    List<Service> findByCategorieServiceIdAndEtatTrueOrderByLibelleAsc(Long idCategorie);
    List<Service> findByCategorieServiceIdOrderByLibelleAsc(Long idCategorie);
    List<Service> findByParentIdAndEtatTrueOrderByLibelleAsc(Long idParent);
    List<Service> findByProprietaireId(Long idProprietaire);
    long countByCategorieServiceId(Long idCategorie);
    long countByParentId(Long idParent);

    @EntityGraph(attributePaths = {"categorieService", "parent"})
    Optional<Service> findWithParentsById(Long id);

    @EntityGraph(attributePaths = {"categorieService", "parent"})
    List<Service> findAllByOrderByCategorieServiceLibelleAscLibelleAsc();

    @EntityGraph(attributePaths = {"categorieService", "informationService",
            "informationService.position", "informationService.position.ville"})
    @org.springframework.data.jpa.repository.Query(
        "select s from Service s where s.etat = true and s.valide = true and s.bloque = false "
      + "and s.informationService.position is not null "
      + "order by s.libelle")
    List<Service> findGeolocalises();

    /** Services créés par un fournisseur et en attente de validation admin. */
    @EntityGraph(attributePaths = {"categorieService", "proprietaire"})
    List<Service> findByValideFalseAndEtatTrueOrderByDateCreationAsc();

    @EntityGraph(attributePaths = {"categorieService", "proprietaire",
            "informationService", "informationService.position", "informationService.position.ville",
            "informationService.horaires", "informationService.horaires.jour"})
    Optional<Service> findVitrineById(Long id);
}
