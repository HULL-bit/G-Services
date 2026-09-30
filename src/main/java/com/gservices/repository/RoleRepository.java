package com.gservices.repository;

import com.gservices.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RoleRepository
        extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    List<Role> findByPersonneId(Long idPersonne);

    List<Role> findByProfilId(Long idProfil);

    Optional<Role> findByPersonneIdAndProfilId(Long idPersonne, Long idProfil);

    boolean existsByPersonneIdAndProfilId(Long idPersonne, Long idProfil);
}
