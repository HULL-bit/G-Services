package com.gservices.repository;

import com.gservices.entity.Profil;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProfilRepository
        extends JpaRepository<Profil, Long>, JpaSpecificationExecutor<Profil> {

    Optional<Profil> findByLibelleIgnoreCase(String libelle);

    boolean existsByLibelleIgnoreCase(String libelle);

    @EntityGraph(attributePaths = {"roleProfils", "roleProfils.permission", "branchePermissions"})
    Optional<Profil> findWithDetailsById(Long id);
}
