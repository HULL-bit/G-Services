package com.gservices.mapper;

import com.gservices.dto.BranchePermissionDto;
import com.gservices.entity.BranchePermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BranchePermissionMapper {

    @Mapping(target = "nbPermissions", expression = "java(b.getPermissions() == null ? 0L : (long) b.getPermissions().size())")
    BranchePermissionDto toDto(BranchePermission b);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "dateMaj", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "profils", ignore = true)
    BranchePermission toEntity(BranchePermissionDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "dateMaj", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "profils", ignore = true)
    void update(@MappingTarget BranchePermission target, BranchePermissionDto dto);
}
