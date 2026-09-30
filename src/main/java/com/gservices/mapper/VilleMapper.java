package com.gservices.mapper;

import com.gservices.dto.VilleDto;
import com.gservices.entity.Ville;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = PositionMapper.class)
public interface VilleMapper {

    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionLibelle", source = "region.libelle")
    @Mapping(target = "paysId", expression = "java(v.getRegion() != null && v.getRegion().getPays() != null ? v.getRegion().getPays().getId() : null)")
    @Mapping(target = "paysLibelle", expression = "java(v.getRegion() != null && v.getRegion().getPays() != null ? v.getRegion().getPays().getLibelle() : null)")
    @Mapping(target = "continentId", expression = "java(continentId(v))")
    @Mapping(target = "nbPositions", expression = "java(v.getPositions() == null ? 0L : (long) v.getPositions().size())")
    VilleDto toDto(Ville v);

    default Long continentId(Ville v) {
        if (v.getRegion() != null && v.getRegion().getPays() != null
                && v.getRegion().getPays().getZoneGeographique() != null
                && v.getRegion().getPays().getZoneGeographique().getContinent() != null) {
            return v.getRegion().getPays().getZoneGeographique().getContinent().getId();
        }
        return null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "positions", ignore = true)
    Ville toEntity(VilleDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "positions", ignore = true)
    void update(@MappingTarget Ville target, VilleDto dto);
}
