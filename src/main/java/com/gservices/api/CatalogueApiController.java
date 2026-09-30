package com.gservices.api;

import com.gservices.api.dto.ApiDtos.AvisRequest;
import com.gservices.dto.*;
import com.gservices.service.AvisService;
import com.gservices.service.VitrineService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Données publiques consommées par l'app mobile : catégories, services, fiche, avis. */
@RestController
@RequestMapping("/api")
public class CatalogueApiController {

    private final VitrineService vitrine;
    private final AvisService avisService;

    public CatalogueApiController(VitrineService vitrine, AvisService avisService) {
        this.vitrine = vitrine;
        this.avisService = avisService;
    }

    @GetMapping("/categories")
    public List<CategorieServiceDto> categories() {
        return vitrine.categories();
    }

    /** Liste de services : par catégorie ({@code categorieId}), en avant ({@code featured=N}), ou tout géolocalisé. */
    @GetMapping("/services")
    public List<ServiceVitrineDto> services(@RequestParam(required = false) Long categorieId,
                                            @RequestParam(required = false) Integer featured) {
        if (categorieId != null) {
            return vitrine.servicesParCategorie(categorieId);
        }
        if (featured != null) {
            return vitrine.servicesEnAvant(Math.max(1, Math.min(featured, 50)));
        }
        return vitrine.servicesGeolocalises();
    }

    @GetMapping("/services/geo")
    public List<ServiceVitrineDto> servicesGeo() {
        return vitrine.servicesGeolocalises();
    }

    @GetMapping("/services/{id}")
    public ServiceDetailDto serviceDetail(@PathVariable Long id) {
        return vitrine.serviceDetail(id);
    }

    @GetMapping("/services/{id}/avis")
    public List<AvisDto> avis(@PathVariable Long id) {
        return vitrine.serviceDetail(id).getAvis();
    }

    @PostMapping("/services/{id}/avis")
    @PreAuthorize("isAuthenticated()")
    public AvisDto deposerAvis(@PathVariable Long id, @Valid @RequestBody AvisRequest req) {
        return avisService.deposerAvisService(id, req.note(), req.commentaire());
    }
}
