package com.gservices.mapper;

import com.gservices.dto.AvisDto;
import com.gservices.entity.Avis;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvisMapper {

    @Mapping(target = "personneId", source = "personne.id")
    @Mapping(target = "personneNom", expression = "java(a.getPersonne() != null ? a.getPersonne().getNomComplet() : null)")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceLibelle", source = "service.libelle")
    @Mapping(target = "produitId", source = "produit.id")
    @Mapping(target = "produitLibelle", source = "produit.libelle")
    @Mapping(target = "anneeId", source = "annee.id")
    @Mapping(target = "anneeValeur", source = "annee.valeurAnnee")
    @Mapping(target = "cibleType", expression = "java(a.getService() != null ? \"SERVICE\" : \"PRODUIT\")")
    @Mapping(target = "cibleLibelle", expression = "java(a.getService() != null ? a.getService().getLibelle() : (a.getProduit() != null ? a.getProduit().getLibelle() : null))")
    AvisDto toDto(Avis a);
}
