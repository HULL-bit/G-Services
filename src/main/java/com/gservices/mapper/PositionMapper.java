package com.gservices.mapper;

import com.gservices.dto.PositionDto;
import com.gservices.entity.Position;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PositionMapper {

    @Mapping(target = "villeId", source = "ville.id")
    @Mapping(target = "villeLibelle", source = "ville.libelle")
    PositionDto toDto(Position p);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ville", ignore = true)
    Position toEntity(PositionDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ville", ignore = true)
    void update(@MappingTarget Position target, PositionDto dto);
}
