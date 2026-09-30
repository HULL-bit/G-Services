package com.gservices.mapper;

import com.gservices.dto.ProfilDto;
import com.gservices.entity.Profil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.LinkedHashSet;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfilMapper {

    @Mapping(target = "permissionIds", expression = "java(permissionIds(profil))")
    @Mapping(target = "brancheIds", expression = "java(brancheIds(profil))")
    @Mapping(target = "nbPersonnes", expression = "java(nbPersonnes(profil))")
    ProfilDto toDto(Profil profil);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "dateMaj", ignore = true)
    @Mapping(target = "roleProfils", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "branchePermissions", ignore = true)
    Profil toEntity(ProfilDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "dateMaj", ignore = true)
    @Mapping(target = "roleProfils", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "branchePermissions", ignore = true)
    void update(@MappingTarget Profil target, ProfilDto dto);

    default Set<Long> permissionIds(Profil p) {
        Set<Long> ids = new LinkedHashSet<>();
        if (p.getRoleProfils() != null) {
            p.getRoleProfils().stream()
                    .filter(rp -> rp.isEtat() && rp.getPermission() != null)
                    .forEach(rp -> ids.add(rp.getPermission().getId()));
        }
        return ids;
    }

    default Set<Long> brancheIds(Profil p) {
        Set<Long> ids = new LinkedHashSet<>();
        if (p.getBranchePermissions() != null) {
            p.getBranchePermissions().forEach(b -> ids.add(b.getId()));
        }
        return ids;
    }

    default long nbPersonnes(Profil p) {
        return p.getRoles() == null ? 0L
                : p.getRoles().stream().filter(r -> r.isEtat()).count();
    }
}
