package com.gservices.repository;

import com.gservices.entity.Jour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourRepository extends JpaRepository<Jour, Long> {
    List<Jour> findAllByOrderByNumeroJourSemaineAsc();
}
