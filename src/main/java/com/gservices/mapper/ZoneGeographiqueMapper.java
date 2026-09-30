package com.gservices.mapper;

import com.gservices.dto.ZoneGeographiqueDto;
import com.gservices.entity.ZoneGeographique;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ZoneGeographiqueMapper {

    @Mapping(target = "continentId", source = "continent.id")
    @Mapping(target = "continentLibelle", source = "continent.libelle")
    @Mapping(target = "nbPays", expression = "java(z.getPays() == null ? 0L : (long) z.getPays().size())")
    ZoneGeographiqueDto toDto(ZoneGeographique z);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "continent", ignore = true)
    @Mapping(target = "pays", ignore = true)
    ZoneGeographique toEntity(ZoneGeographiqueDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "continent", ignore = true)
    @Mapping(target = "pays", ignore = true)
    void update(@MappingTarget ZoneGeographique target, ZoneGeographiqueDto dto);
}
