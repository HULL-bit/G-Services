package com.gservices.repository;

import com.gservices.entity.Avis;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AvisRepository
        extends JpaRepository<Avis, Long>, JpaSpecificationExecutor<Avis> {

    boolean existsByPersonneIdAndServiceId(Long idPersonne, Long idService);
    boolean existsByPersonneIdAndProduitId(Long idPersonne, Long idProduit);

    @EntityGraph(attributePaths = {"personne", "service", "service.proprietaire", "produit", "annee"})
    Optional<Avis> findDetailById(Long id);

    @EntityGraph(attributePaths = {"personne"})
    List<Avis> findByServiceIdAndEstModereTrueAndEtatTrueOrderByDateDesc(Long idService);

    @EntityGraph(attributePaths = {"personne"})
    List<Avis> findByProduitIdAndEstModereTrueAndEtatTrueOrderByDateDesc(Long idProduit);

    @Query("select coalesce(avg(a.note), 0) from Avis a where a.service.id = :id and a.estModere = true and a.etat = true")
    double moyenneService(Long id);

    @Query("select coalesce(avg(a.note), 0) from Avis a where a.produit.id = :id and a.estModere = true and a.etat = true")
    double moyenneProduit(Long id);

    @Query("select a.note, count(a) from Avis a where a.service.id = :id and a.estModere = true and a.etat = true group by a.note")
    List<Object[]> repartitionService(Long id);

    @Query("select a.note, count(a) from Avis a where a.produit.id = :id and a.estModere = true and a.etat = true group by a.note")
    List<Object[]> repartitionProduit(Long id);

    long countByEstModereFalseAndEtatTrue();

    long countByEstModereTrueAndEtatTrue();
}
