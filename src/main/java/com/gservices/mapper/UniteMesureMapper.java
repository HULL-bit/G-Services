package com.gservices.mapper;

import com.gservices.dto.UniteMesureDto;
import com.gservices.entity.UniteMesure;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UniteMesureMapper {

    UniteMesureDto toDto(UniteMesure u);

    @Mapping(target = "id", ignore = true)
    UniteMesure toEntity(UniteMesureDto dto);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget UniteMesure target, UniteMesureDto dto);
}
