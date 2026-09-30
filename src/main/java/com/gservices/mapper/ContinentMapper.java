package com.gservices.mapper;

import com.gservices.dto.ContinentDto;
import com.gservices.entity.Continent;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContinentMapper {

    @Mapping(target = "nbZones", expression = "java(c.getZones() == null ? 0L : (long) c.getZones().size())")
    ContinentDto toDto(Continent c);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "zones", ignore = true)
    Continent toEntity(ContinentDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "zones", ignore = true)
    void update(@MappingTarget Continent target, ContinentDto dto);
}
