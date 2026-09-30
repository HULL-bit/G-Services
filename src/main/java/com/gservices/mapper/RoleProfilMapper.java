package com.gservices.mapper;

import com.gservices.dto.RoleProfilDto;
import com.gservices.entity.RoleProfil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleProfilMapper {

    @Mapping(target = "profilId", source = "profil.id")
    @Mapping(target = "profilLibelle", source = "profil.libelle")
    @Mapping(target = "permissionId", source = "permission.id")
    @Mapping(target = "permissionCode", source = "permission.code")
    RoleProfilDto toDto(RoleProfil rp);
}
