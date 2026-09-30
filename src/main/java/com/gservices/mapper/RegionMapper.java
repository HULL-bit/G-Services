package com.gservices.mapper;

import com.gservices.dto.RegionDto;
import com.gservices.entity.Region;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegionMapper {

    @Mapping(target = "paysId", source = "pays.id")
    @Mapping(target = "paysLibelle", source = "pays.libelle")
    @Mapping(target = "nbVilles", expression = "java(r.getVilles() == null ? 0L : (long) r.getVilles().size())")
    RegionDto toDto(Region r);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "pays", ignore = true)
    @Mapping(target = "villes", ignore = true)
    Region toEntity(RegionDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "pays", ignore = true)
    @Mapping(target = "villes", ignore = true)
    void update(@MappingTarget Region target, RegionDto dto);
}
