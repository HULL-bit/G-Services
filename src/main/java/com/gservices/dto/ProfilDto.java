package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class ProfilDto implements Serializable {

    private Long id;

    @NotBlank @Size(max = 80)
    private String libelle;

    @Size(max = 255)
    private String description;

    @PositiveOrZero
    private int niveau = 100;

    private LocalDateTime date;
    private LocalDateTime dateMaj;
    private boolean etat = true;

    /** Ids des permissions affectées (édition des droits du profil). */
    private Set<Long> permissionIds = new LinkedHashSet<>();

    /** Ids des branches de menu visibles. */
    private Set<Long> brancheIds = new LinkedHashSet<>();

    private long nbPersonnes;
}
