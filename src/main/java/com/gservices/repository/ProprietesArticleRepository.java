package com.gservices.repository;

import com.gservices.entity.ProprietesArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProprietesArticleRepository
        extends JpaRepository<ProprietesArticle, Long>, JpaSpecificationExecutor<ProprietesArticle> {
    List<ProprietesArticle> findByEtatTrueOrderByLibelleAsc();
    long countByUniteMesureId(Long idUnite);
}
