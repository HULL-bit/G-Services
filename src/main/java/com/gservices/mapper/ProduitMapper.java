package com.gservices.mapper;

import com.gservices.dto.ProduitDto;
import com.gservices.entity.Produit;
import com.gservices.entity.ProprietesArticle;
import org.mapstruct.*;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProduitMapper {

    @Mapping(target = "catalogueId", source = "catalogue.id")
    @Mapping(target = "catalogueLibelle", source = "catalogue.libelle")
    @Mapping(target = "serviceLibelle", expression = "java(p.getCatalogue() != null && p.getCatalogue().getService() != null ? p.getCatalogue().getService().getLibelle() : null)")
    @Mapping(target = "nbArticles", expression = "java(p.getArticles() == null ? 0L : (long) p.getArticles().size())")
    @Mapping(target = "proprieteIds", expression = "java(idsProprietes(p))")
    @Mapping(target = "proprietesLibelles", expression = "java(libellesProprietes(p))")
    ProduitDto toDto(Produit p);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "catalogue", ignore = true)
    @Mapping(target = "articles", ignore = true)
    @Mapping(target = "proprietes", ignore = true)
    Produit toEntity(ProduitDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "catalogue", ignore = true)
    @Mapping(target = "articles", ignore = true)
    @Mapping(target = "proprietes", ignore = true)
    void update(@MappingTarget Produit target, ProduitDto dto);

    default java.util.Set<Long> idsProprietes(Produit p) {
        if (p.getProprietes() == null) {
            return new java.util.LinkedHashSet<>();
        }
        return p.getProprietes().stream().map(ProprietesArticle::getId)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    default String libellesProprietes(Produit p) {
        if (p.getProprietes() == null || p.getProprietes().isEmpty()) {
            return "";
        }
        return p.getProprietes().stream().map(ProprietesArticle::getLibelle)
                .collect(Collectors.joining(", "));
    }
}
