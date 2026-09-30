package com.gservices.repository;

import com.gservices.entity.Mois;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoisRepository extends JpaRepository<Mois, Long> {
    List<Mois> findByAnneeIdOrderByNumeroMoisAsc(Long idAnnee);
}
