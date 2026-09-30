package com.gservices.mapper;

import com.gservices.dto.ArticleDto;
import com.gservices.entity.Article;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleMapper {

    @Mapping(target = "produitId", source = "produit.id")
    @Mapping(target = "produitLibelle", source = "produit.libelle")
    @Mapping(target = "prixNet", expression = "java(a.getPrixNet())")
    @Mapping(target = "nbVarietes", expression = "java(a.getVarietes() == null ? 0L : (long) a.getVarietes().size())")
    ArticleDto toDto(Article a);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "produit", ignore = true)
    @Mapping(target = "varietes", ignore = true)
    Article toEntity(ArticleDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "produit", ignore = true)
    @Mapping(target = "varietes", ignore = true)
    void update(@MappingTarget Article target, ArticleDto dto);
}
