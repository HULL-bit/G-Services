package com.gservices.mapper;

import com.gservices.dto.InformationServiceDto;
import com.gservices.entity.InformationService;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InformationServiceMapper {

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceLibelle", source = "service.libelle")
    @Mapping(target = "categorieLibelle", expression = "java(i.getService() != null && i.getService().getCategorieService() != null ? i.getService().getCategorieService().getLibelle() : null)")
    @Mapping(target = "positionId", source = "position.id")
    @Mapping(target = "positionLibelle", expression = "java(i.getPosition() != null && i.getPosition().getVille() != null ? i.getPosition().getVille().getLibelle() : null)")
    @Mapping(target = "latitude", source = "position.latitude")
    @Mapping(target = "longitude", source = "position.longitude")
    @Mapping(target = "nbHoraires", expression = "java(i.getHoraires() == null ? 0L : (long) i.getHoraires().size())")
    InformationServiceDto toDto(InformationService i);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "dateMaj", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "horaires", ignore = true)
    InformationService toEntity(InformationServiceDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "dateMaj", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "horaires", ignore = true)
    void update(@MappingTarget InformationService target, InformationServiceDto dto);
}
