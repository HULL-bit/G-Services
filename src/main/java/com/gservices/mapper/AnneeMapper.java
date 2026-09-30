package com.gservices.mapper;

import com.gservices.dto.AnneeDto;
import com.gservices.entity.Annee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnneeMapper {

    @Mapping(target = "nbMois", expression = "java(a.getMois() == null ? 0L : (long) a.getMois().size())")
    AnneeDto toDto(Annee a);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mois", ignore = true)
    Annee toEntity(AnneeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mois", ignore = true)
    void update(@MappingTarget Annee target, AnneeDto dto);
}
