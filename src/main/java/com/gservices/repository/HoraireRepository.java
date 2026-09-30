package com.gservices.repository;

import com.gservices.entity.Horaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoraireRepository extends JpaRepository<Horaire, Long> {
    List<Horaire> findByInformationServiceIdOrderByJourNumeroJourSemaineAsc(Long idInfoService);
    Optional<Horaire> findByInformationServiceIdAndJourId(Long idInfoService, Long idJour);
    long countByJourId(Long idJour);
}
