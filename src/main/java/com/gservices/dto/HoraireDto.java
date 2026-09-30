package com.gservices.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class HoraireDto implements Serializable {
    private Long id;
    @Size(max = 5) private String heureOuverture;
    @Size(max = 5) private String heureFermeture;
    @Size(max = 5) private String pauseDejeunerDebut;
    @Size(max = 5) private String pauseDejeunerFin;
    private boolean ouvert24h;
    private boolean etat = true;
    private Long informationServiceId;
    @NotNull private Long jourId;
    private String jourLibelle;
    private int numeroJour;
}
