package com.gservices.mapper;

import com.gservices.dto.LotDto;
import com.gservices.entity.Lot;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LotMapper {

    @Mapping(target = "stockId", source = "stock.id")
    @Mapping(target = "moisId", source = "mois.id")
    @Mapping(target = "moisLibelle", source = "mois.libelle")
    @Mapping(target = "anneeValeur", expression = "java(l.getMois() != null && l.getMois().getAnnee() != null ? l.getMois().getAnnee().getValeurAnnee() : null)")
    @Mapping(target = "perime", expression = "java(l.isPerime())")
    LotDto toDto(Lot l);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "mois", ignore = true)
    Lot toEntity(LotDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "mois", ignore = true)
    void update(@MappingTarget Lot target, LotDto dto);
}
