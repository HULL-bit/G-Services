package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PermissionDto implements Serializable {

    private Long id;

    @NotBlank @Size(max = 100)
    private String code;

    @NotBlank @Size(max = 120)
    private String libelle;

    @Size(max = 40)
    private String actionAutorisee;

    @Size(max = 150)
    private String reference;

    @PositiveOrZero
    private int niveau = 0;

    private LocalDateTime date;
    private LocalDateTime dateMaj;
    private boolean etat = true;

    private Long brancheId;
    private String brancheCode;
    private String brancheLibelle;
}
