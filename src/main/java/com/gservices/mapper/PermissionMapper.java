package com.gservices.mapper;

import com.gservices.dto.PermissionDto;
import com.gservices.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    @Mapping(target = "brancheId", source = "branchePermission.id")
    @Mapping(target = "brancheCode", source = "branchePermission.code")
    @Mapping(target = "brancheLibelle", source = "branchePermission.libelle")
    PermissionDto toDto(Permission p);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "dateMaj", ignore = true)
    @Mapping(target = "branchePermission", ignore = true)
    @Mapping(target = "roleProfils", ignore = true)
    Permission toEntity(PermissionDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "dateMaj", ignore = true)
    @Mapping(target = "branchePermission", ignore = true)
    @Mapping(target = "roleProfils", ignore = true)
    void update(@MappingTarget Permission target, PermissionDto dto);
}
