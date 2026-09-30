package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UniteMesureDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 60) private String libelle;
    @Size(max = 12) private String symbole;
    private boolean etat = true;
    private long nbProprietes;
}
