package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class CatalogueDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 120) private String libelle;
    @Size(max = 500) private String description;
    @Size(max = 60) private String icone;
    private int ordreAffichage;
    private boolean etat = true;
    @NotNull private Long serviceId;
    private String serviceLibelle;
    private long nbProduits;
}
