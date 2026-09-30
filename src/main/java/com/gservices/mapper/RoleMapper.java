package com.gservices.mapper;

import com.gservices.dto.RoleDto;
import com.gservices.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "personneId", source = "personne.id")
    @Mapping(target = "personneNomComplet", expression = "java(r.getPersonne() == null ? null : r.getPersonne().getNomComplet())")
    @Mapping(target = "profilId", source = "profil.id")
    @Mapping(target = "profilLibelle", source = "profil.libelle")
    RoleDto toDto(Role r);
}
