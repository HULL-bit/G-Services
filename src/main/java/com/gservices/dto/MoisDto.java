package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class MoisDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 20) private String libelle;
    @Size(max = 5) private String abreviation;
    private int numeroMois;
    private int nombreJours;
    private boolean etat = true;
    private Long anneeId;
    private Integer anneeValeur;
}
