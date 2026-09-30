package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ServiceDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 120) private String libelle;
    @Size(max = 500) private String description;
    @Size(max = 160) private String image;
    private Double prixIndicatif;
    private boolean commandeADistance;
    private boolean etat = true;
    /** Validé par un administrateur — sinon invisible sur le front public. */
    private boolean valide = true;
    @NotNull private Long categorieServiceId;
    private String categorieServiceLibelle;
    private Long parentId;
    private String parentLibelle;
    private Long proprietaireId;
    private String proprietaireNom;
    private long nbSousServices;
    private long nbCatalogues;
}
