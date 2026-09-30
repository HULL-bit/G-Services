package com.gservices.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** DTO de la classe d'association {@link com.gservices.entity.RoleProfil} (Profil × Permission). */
@Data
public class RoleProfilDto implements Serializable {

    private Long id;

    @NotNull
    private Long profilId;
    private String profilLibelle;

    @NotNull
    private Long permissionId;
    private String permissionCode;

    private LocalDateTime dateAffectation;
    private LocalDateTime dateMaj;
    private boolean etat = true;
}
