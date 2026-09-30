package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class CategorieServiceDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 100) private String libelle;
    @Size(max = 255) private String description;
    @Size(max = 60) private String icone;
    private int ordreAffichage;
    private boolean etat = true;
    private long nbServices;
}
