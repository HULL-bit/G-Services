package com.gservices.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class PositionDto implements Serializable {
    private Long id;
    @NotNull private Double latitude;
    @NotNull private Double longitude;
    private Double altitude;
    private Double precisionMetres;
    private LocalDate dateReleve;
    private boolean etat = true;
    private Long villeId;
    private String villeLibelle;
}
