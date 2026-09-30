package com.gservices.repository;

import com.gservices.entity.Annee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AnneeRepository
        extends JpaRepository<Annee, Long>, JpaSpecificationExecutor<Annee> {
    boolean existsByValeurAnnee(int valeurAnnee);
    Optional<Annee> findByValeurAnnee(int valeurAnnee);
    List<Annee> findAllByOrderByValeurAnneeDesc();
}
