package com.gservices.repository;

import com.gservices.entity.Commande;
import com.gservices.entity.StatutCommande;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CommandeRepository
        extends JpaRepository<Commande, Long>, JpaSpecificationExecutor<Commande> {

    @EntityGraph(attributePaths = {"client", "service", "service.proprietaire",
            "lignes", "lignes.article", "lignes.article.produit"})
    Optional<Commande> findDetailById(Long id);

    @EntityGraph(attributePaths = {"service"})
    List<Commande> findByClientIdAndEtatTrueOrderByDateCommandeDesc(Long idClient);

    long countByServiceProprietaireIdAndStatut(Long idProprietaire, StatutCommande statut);

    long countByStatutAndEtatTrue(StatutCommande statut);

    long countByStatutNotInAndEtatTrue(java.util.Collection<StatutCommande> statuts);

    long countByEtatTrue();

    @org.springframework.data.jpa.repository.Query(
        "select coalesce(sum(c.montantTotal), 0) from Commande c where c.etat = true")
    java.math.BigDecimal montantTotal();
}
