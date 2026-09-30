package com.gservices.mapper;

import com.gservices.dto.CategorieServiceDto;
import com.gservices.entity.CategorieService;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategorieServiceMapper {

    @Mapping(target = "nbServices", expression = "java(c.getServices() == null ? 0L : (long) c.getServices().size())")
    CategorieServiceDto toDto(CategorieService c);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "services", ignore = true)
    CategorieService toEntity(CategorieServiceDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "services", ignore = true)
    void update(@MappingTarget CategorieService target, CategorieServiceDto dto);
}
