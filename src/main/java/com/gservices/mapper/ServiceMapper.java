package com.gservices.mapper;

import com.gservices.dto.ServiceDto;
import com.gservices.entity.Service;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceMapper {

    @Mapping(target = "categorieServiceId", source = "categorieService.id")
    @Mapping(target = "categorieServiceLibelle", source = "categorieService.libelle")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentLibelle", source = "parent.libelle")
    @Mapping(target = "proprietaireId", source = "proprietaire.id")
    @Mapping(target = "proprietaireNom", expression = "java(s.getProprietaire() != null ? s.getProprietaire().getNomComplet() : null)")
    @Mapping(target = "nbSousServices", expression = "java(s.getSousServices() == null ? 0L : (long) s.getSousServices().size())")
    @Mapping(target = "nbCatalogues", expression = "java(s.getCatalogues() == null ? 0L : (long) s.getCatalogues().size())")
    ServiceDto toDto(Service s);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "valide", ignore = true)
    @Mapping(target = "categorieService", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "proprietaire", ignore = true)
    @Mapping(target = "sousServices", ignore = true)
    @Mapping(target = "catalogues", ignore = true)
    Service toEntity(ServiceDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "valide", ignore = true)
    @Mapping(target = "categorieService", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "proprietaire", ignore = true)
    @Mapping(target = "sousServices", ignore = true)
    @Mapping(target = "catalogues", ignore = true)
    void update(@MappingTarget Service target, ServiceDto dto);
}
