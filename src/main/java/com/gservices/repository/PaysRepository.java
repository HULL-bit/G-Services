package com.gservices.repository;

import com.gservices.entity.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaysRepository
        extends JpaRepository<Pays, Long>, JpaSpecificationExecutor<Pays> {
    boolean existsByCodeIso2IgnoreCase(String codeIso2);
    boolean existsByCodeIso3IgnoreCase(String codeIso3);
    Optional<Pays> findByCodeIso2IgnoreCase(String codeIso2);
    List<Pays> findByZoneGeographiqueIdAndEtatTrueOrderByLibelleAsc(Long idZone);
    List<Pays> findByEtatTrueOrderByLibelleAsc();
    @Query("select p from Pays p where p.etat = true and p.zoneGeographique.continent.id = :idContinent order by p.libelle")
    List<Pays> findActifsParContinent(Long idContinent);
    long countByZoneGeographiqueId(Long idZone);
}
