package com.gservices.repository;

import com.gservices.entity.Favori;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriRepository extends JpaRepository<Favori, Long> {

    Optional<Favori> findByPersonneIdAndServiceId(Long idPersonne, Long idService);

    boolean existsByPersonneIdAndServiceIdAndEtatTrue(Long idPersonne, Long idService);

    long countByServiceIdAndEtatTrue(Long idService);

    @EntityGraph(attributePaths = {"service", "service.categorieService"})
    List<Favori> findByPersonneIdAndEtatTrueOrderByDateAjoutDesc(Long idPersonne);
}
