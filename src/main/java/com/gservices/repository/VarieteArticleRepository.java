package com.gservices.repository;

import com.gservices.entity.VarieteArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VarieteArticleRepository extends JpaRepository<VarieteArticle, Long> {
    List<VarieteArticle> findByArticleIdOrderByProprietesArticleLibelleAsc(Long idArticle);
    Optional<VarieteArticle> findByArticleIdAndProprietesArticleId(Long idArticle, Long idPropriete);
    long countByProprietesArticleId(Long idPropriete);
}
