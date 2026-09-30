package com.gservices.repository;

import com.gservices.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PositionRepository
        extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {
    List<Position> findByVilleId(Long idVille);
}
