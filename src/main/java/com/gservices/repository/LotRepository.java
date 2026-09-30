package com.gservices.repository;

import com.gservices.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotRepository extends JpaRepository<Lot, Long> {
    List<Lot> findByStockIdOrderByDatePeremptionAsc(Long idStock);
    long countByMoisId(Long idMois);
}
