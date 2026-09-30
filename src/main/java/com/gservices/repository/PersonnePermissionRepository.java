package com.gservices.repository;

import com.gservices.entity.PersonnePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonnePermissionRepository extends JpaRepository<PersonnePermission, Long> {

    Optional<PersonnePermission> findByPersonneIdAndPermissionId(Long idPersonne, Long idPermission);
}
