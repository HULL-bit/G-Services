package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class JourDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 20) private String libelle;
    @Size(max = 5) private String abreviation;
    private int numeroJourSemaine;
    private boolean estWeekend;
    private boolean estFerie;
    private boolean etat = true;
}
