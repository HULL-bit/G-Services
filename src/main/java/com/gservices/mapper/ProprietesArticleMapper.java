package com.gservices.mapper;

import com.gservices.dto.ProprietesArticleDto;
import com.gservices.entity.ProprietesArticle;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProprietesArticleMapper {

    @Mapping(target = "uniteMesureId", source = "uniteMesure.id")
    @Mapping(target = "uniteMesureLibelle", source = "uniteMesure.libelle")
    @Mapping(target = "uniteMesureSymbole", source = "uniteMesure.symbole")
    ProprietesArticleDto toDto(ProprietesArticle p);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uniteMesure", ignore = true)
    @Mapping(target = "varietes", ignore = true)
    @Mapping(target = "produits", ignore = true)
    ProprietesArticle toEntity(ProprietesArticleDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uniteMesure", ignore = true)
    @Mapping(target = "varietes", ignore = true)
    @Mapping(target = "produits", ignore = true)
    void update(@MappingTarget ProprietesArticle target, ProprietesArticleDto dto);
}
