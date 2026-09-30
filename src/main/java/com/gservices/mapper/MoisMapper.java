package com.gservices.mapper;

import com.gservices.dto.MoisDto;
import com.gservices.entity.Mois;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MoisMapper {

    @Mapping(target = "anneeId", source = "annee.id")
    @Mapping(target = "anneeValeur", source = "annee.valeurAnnee")
    MoisDto toDto(Mois m);
}
