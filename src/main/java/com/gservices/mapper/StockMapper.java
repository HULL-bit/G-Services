package com.gservices.mapper;

import com.gservices.dto.StockDto;
import com.gservices.entity.Lot;
import com.gservices.entity.Stock;
import com.gservices.entity.Service;
import org.mapstruct.*;

import java.time.LocalDate;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockMapper {

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleReference", source = "article.reference")
    @Mapping(target = "produitLibelle", expression = "java(s.getArticle() != null && s.getArticle().getProduit() != null ? s.getArticle().getProduit().getLibelle() : null)")
    @Mapping(target = "serviceLibelle", expression = "java(serviceDe(s) != null ? serviceDe(s).getLibelle() : null)")
    @Mapping(target = "categorieLibelle", expression = "java(serviceDe(s) != null && serviceDe(s).getCategorieService() != null ? serviceDe(s).getCategorieService().getLibelle() : null)")
    @Mapping(target = "proprietaireNom", expression = "java(serviceDe(s) != null && serviceDe(s).getProprietaire() != null ? serviceDe(s).getProprietaire().getNomComplet() : null)")
    @Mapping(target = "quantiteDisponible", expression = "java(s.getQuantiteDisponible())")
    @Mapping(target = "enAlerte", expression = "java(s.isEnAlerte())")
    @Mapping(target = "nbLots", expression = "java(s.getLots() == null ? 0L : (long) s.getLots().size())")
    @Mapping(target = "nbLotsPerimes", expression = "java(nbPerimes(s))")
    StockDto toDto(Stock s);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "lots", ignore = true)
    Stock toEntity(StockDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "lots", ignore = true)
    void update(@MappingTarget Stock target, StockDto dto);

    default Service serviceDe(Stock s) {
        if (s.getArticle() == null || s.getArticle().getProduit() == null
                || s.getArticle().getProduit().getCatalogue() == null) {
            return null;
        }
        return s.getArticle().getProduit().getCatalogue().getService();
    }

    default long nbPerimes(Stock s) {
        if (s.getLots() == null) {
            return 0L;
        }
        LocalDate today = LocalDate.now();
        return s.getLots().stream()
                .filter(l -> l.getDatePeremption() != null && l.getDatePeremption().isBefore(today))
                .map(Lot::getId).count();
    }
}
