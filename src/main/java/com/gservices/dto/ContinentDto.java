package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ContinentDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 80) private String libelle;
    @Size(max = 10) private String code;
    private Double superficieKm2;
    private Long population;
    private boolean etat = true;
    private long nbZones;
}
