package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class ProduitDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 150) private String libelle;
    @Size(max = 1000) private String description;
    @Size(max = 160) private String image;
    private boolean etat = true;
    @NotNull private Long catalogueId;
    private String catalogueLibelle;
    private String serviceLibelle;
    private long nbArticles;
    /** Propriétés déclarées (association « définit »). */
    private Set<Long> proprieteIds = new LinkedHashSet<>();
    private String proprietesLibelles;
}
