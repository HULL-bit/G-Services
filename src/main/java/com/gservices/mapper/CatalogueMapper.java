package com.gservices.mapper;

import com.gservices.dto.CatalogueDto;
import com.gservices.entity.Catalogue;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CatalogueMapper {

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceLibelle", source = "service.libelle")
    @Mapping(target = "nbProduits", expression = "java(c.getProduits() == null ? 0L : (long) c.getProduits().size())")
    CatalogueDto toDto(Catalogue c);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "produits", ignore = true)
    Catalogue toEntity(CatalogueDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "produits", ignore = true)
    void update(@MappingTarget Catalogue target, CatalogueDto dto);
}
