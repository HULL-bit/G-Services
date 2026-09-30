package com.gservices.mapper;

import com.gservices.dto.PersonneDto;
import com.gservices.entity.Personne;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonneMapper {

    @Mapping(target = "compteVerrouille", expression = "java(p.isCompteVerrouille())")
    @Mapping(target = "profils", expression = "java(libellesProfils(p))")
    @Mapping(target = "categorieSpecialiteId", source = "categorieSpecialite.id")
    @Mapping(target = "categorieSpecialiteLibelle", source = "categorieSpecialite.libelle")
    @Mapping(target = "nouveauMotDePasse", ignore = true)
    PersonneDto toDto(Personne p);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "categorieSpecialite", ignore = true)
    @Mapping(target = "dateInscription", ignore = true)
    @Mapping(target = "derniereConnexion", ignore = true)
    @Mapping(target = "tentativesEchouees", ignore = true)
    @Mapping(target = "verrouilleJusqua", ignore = true)
    Personne toEntity(PersonneDto dto);

    /** MAJ partielle : le login et le mot de passe ne se modifient pas ici. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "login", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "categorieSpecialite", ignore = true)
    @Mapping(target = "dateInscription", ignore = true)
    @Mapping(target = "derniereConnexion", ignore = true)
    @Mapping(target = "tentativesEchouees", ignore = true)
    @Mapping(target = "verrouilleJusqua", ignore = true)
    void update(@MappingTarget Personne target, PersonneDto dto);

    default List<String> libellesProfils(Personne p) {
        if (p.getRoles() == null) {
            return List.of();
        }
        return p.getRoles().stream()
                .filter(r -> r.isEtat() && r.getProfil() != null)
                .map(r -> r.getProfil().getLibelle())
                .distinct()
                .toList();
    }
}
