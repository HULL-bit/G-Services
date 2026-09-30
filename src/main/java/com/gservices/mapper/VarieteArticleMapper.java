package com.gservices.mapper;

import com.gservices.dto.VarieteArticleDto;
import com.gservices.entity.VarieteArticle;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VarieteArticleMapper {

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleReference", source = "article.reference")
    @Mapping(target = "proprietesArticleId", source = "proprietesArticle.id")
    @Mapping(target = "proprietesArticleLibelle", source = "proprietesArticle.libelle")
    @Mapping(target = "typeSaisie", source = "proprietesArticle.typeSaisie")
    @Mapping(target = "uniteMesureSymbole", expression = "java(v.getProprietesArticle() != null && v.getProprietesArticle().getUniteMesure() != null ? v.getProprietesArticle().getUniteMesure().getSymbole() : null)")
    VarieteArticleDto toDto(VarieteArticle v);
}
