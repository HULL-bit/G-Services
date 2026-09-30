package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BranchePermissionDto implements Serializable {

    private Long id;

    @NotBlank @Size(max = 60)
    private String code;

    @NotBlank @Size(max = 100)
    private String libelle;

    @Size(max = 60)
    private String icone;

    @Size(max = 150)
    private String reference;

    @PositiveOrZero
    private int niveau = 0;

    private LocalDateTime date;
    private LocalDateTime dateMaj;
    private boolean etat = true;

    private long nbPermissions;
}
