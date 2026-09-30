package com.gservices.api;

import com.gservices.api.dto.ApiDtos.MessageResponse;
import com.gservices.dto.FavoriDto;
import com.gservices.service.FavoriService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Espace favoris depuis l'app mobile (client connecté). */
@RestController
@RequestMapping("/api")
public class FavoriApiController {

    private final FavoriService favoris;

    public FavoriApiController(FavoriService favoris) {
        this.favoris = favoris;
    }

    @GetMapping("/me/favoris")
    @PreAuthorize("isAuthenticated()")
    public List<FavoriDto> mesFavoris() {
        return favoris.mesFavoris();
    }

    @GetMapping("/services/{id}/favori")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Boolean> estFavori(@PathVariable Long id) {
        return Map.of("favori", favoris.estFavori(id));
    }

    @PostMapping("/services/{id}/favori")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Boolean> basculer(@PathVariable Long id) {
        return Map.of("favori", favoris.basculerFavori(id));
    }

    @DeleteMapping("/services/{id}/favori")
    @PreAuthorize("isAuthenticated()")
    public MessageResponse retirer(@PathVariable Long id) {
        if (favoris.estFavori(id)) {
            favoris.basculerFavori(id);
        }
        return new MessageResponse("Retiré des favoris.");
    }
}
