package com.gservices.web.bean;

import com.gservices.dto.FavoriDto;
import com.gservices.service.FavoriService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Page publique « Mes favoris » (client connecté). */
@Component("favoriBean")
@Scope("view")
public class FavoriBean {

    private final transient FavoriService favoriService;
    private List<FavoriDto> favoris = new ArrayList<>();

    public FavoriBean(FavoriService favoriService) {
        this.favoriService = favoriService;
    }

    @PostConstruct
    void init() {
        try {
            favoris = favoriService.mesFavoris();
        } catch (RuntimeException e) {
            favoris = new ArrayList<>();
        }
    }

    public void retirer(Long idService) {
        favoriService.basculerFavori(idService);
        init();
    }

    public List<FavoriDto> getFavoris() {
        return favoris;
    }
}
