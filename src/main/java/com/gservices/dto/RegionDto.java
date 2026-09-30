package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class RegionDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 100) private String libelle;
    @Size(max = 20) private String codeRegion;
    @Size(max = 100) private String chefLieu;
    private Double superficieKm2;
    private Long population;
    private boolean etat = true;
    @NotNull private Long paysId;
    private String paysLibelle;
    private long nbVilles;
}
