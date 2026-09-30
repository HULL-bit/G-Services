package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class VilleDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 100) private String libelle;
    @Size(max = 20) private String codePostal;
    @Size(max = 20) private String indicatifZone;
    private Long population;
    private Double superficieKm2;
    private boolean estCapitale;
    private boolean etat = true;
    @NotNull private Long regionId;
    private String regionLibelle;
    private Long paysId;
    private String paysLibelle;
    private Long continentId;
    private List<PositionDto> positions;
    private long nbPositions;
}
