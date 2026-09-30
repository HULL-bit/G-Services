package com.gservices.mapper;

import com.gservices.dto.CommandeDto;
import com.gservices.dto.LigneCommandeDto;
import com.gservices.entity.Commande;
import com.gservices.entity.LigneCommande;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommandeMapper {

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceLibelle", source = "service.libelle")
    @Mapping(target = "categorieLibelle", expression = "java(c.getService() != null && c.getService().getCategorieService() != null ? c.getService().getCategorieService().getLibelle() : null)")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientNom", expression = "java(c.getClient() != null ? c.getClient().getNomComplet() : null)")
    @Mapping(target = "proprietaireNom", expression = "java(c.getService() != null && c.getService().getProprietaire() != null ? c.getService().getProprietaire().getNomComplet() : null)")
    @Mapping(target = "nombreArticles", expression = "java(c.getNombreArticles())")
    CommandeDto toDto(Commande c);

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleReference", source = "article.reference")
    @Mapping(target = "articleDescription", source = "article.description")
    @Mapping(target = "sousTotal", expression = "java(l.getSousTotal())")
    LigneCommandeDto toDto(LigneCommande l);
}
