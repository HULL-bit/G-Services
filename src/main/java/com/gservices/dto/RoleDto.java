package com.gservices.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** DTO de la classe d'association {@link com.gservices.entity.Role} (Personne × Profil). */
@Data
public class RoleDto implements Serializable {

    private Long id;

    @NotNull
    private Long personneId;
    private String personneNomComplet;

    @NotNull
    private Long profilId;
    private String profilLibelle;

    private LocalDateTime dateAttribution;
    private LocalDateTime dateMaj;
    private boolean etat = true;
}
