package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ZoneGeographiqueDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 80) private String libelle;
    @Size(max = 10) private String code;
    @Size(max = 255) private String description;
    private boolean etat = true;
    @NotNull private Long continentId;
    private String continentLibelle;
    private long nbPays;
}
