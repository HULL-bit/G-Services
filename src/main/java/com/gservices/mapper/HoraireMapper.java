package com.gservices.mapper;

import com.gservices.dto.HoraireDto;
import com.gservices.entity.Horaire;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HoraireMapper {

    @Mapping(target = "informationServiceId", source = "informationService.id")
    @Mapping(target = "jourId", source = "jour.id")
    @Mapping(target = "jourLibelle", source = "jour.libelle")
    @Mapping(target = "numeroJour", source = "jour.numeroJourSemaine")
    HoraireDto toDto(Horaire h);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "informationService", ignore = true)
    @Mapping(target = "jour", ignore = true)
    Horaire toEntity(HoraireDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "informationService", ignore = true)
    @Mapping(target = "jour", ignore = true)
    void update(@MappingTarget Horaire target, HoraireDto dto);
}
