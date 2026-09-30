package com.gservices.mapper;

import com.gservices.dto.JourDto;
import com.gservices.entity.Jour;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JourMapper {
    JourDto toDto(Jour j);

    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Jour target, JourDto dto);
}
