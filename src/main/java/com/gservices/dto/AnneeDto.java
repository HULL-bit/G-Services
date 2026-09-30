package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class AnneeDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 20) private String libelle;
    @NotNull private Integer valeurAnnee;
    private boolean estBissextile;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean etat = true;
    private long nbMois;
}
