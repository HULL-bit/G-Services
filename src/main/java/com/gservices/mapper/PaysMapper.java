package com.gservices.mapper;

import com.gservices.dto.PaysDto;
import com.gservices.entity.Pays;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaysMapper {

    @Mapping(target = "zoneGeographiqueId", source = "zoneGeographique.id")
    @Mapping(target = "zoneGeographiqueLibelle", source = "zoneGeographique.libelle")
    @Mapping(target = "continentLibelle", expression = "java(p.getZoneGeographique() != null && p.getZoneGeographique().getContinent() != null ? p.getZoneGeographique().getContinent().getLibelle() : null)")
    @Mapping(target = "nbRegions", expression = "java(p.getRegions() == null ? 0L : (long) p.getRegions().size())")
    PaysDto toDto(Pays p);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "zoneGeographique", ignore = true)
    @Mapping(target = "regions", ignore = true)
    Pays toEntity(PaysDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "zoneGeographique", ignore = true)
    @Mapping(target = "regions", ignore = true)
    void update(@MappingTarget Pays target, PaysDto dto);
}
