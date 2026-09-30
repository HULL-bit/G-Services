package com.gservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class InformationServiceDto implements Serializable {
    private Long id;
    @Size(max = 255) private String adresse;
    @Size(max = 30) private String telephone1;
    @Size(max = 30) private String telephone2;
    @Email @Size(max = 120) private String email1;
    @Email @Size(max = 120) private String email2;
    @Size(max = 160) private String siteWeb;
    private boolean disponibilite = true;
    private boolean etat = true;
    @NotNull private Long serviceId;
    private String serviceLibelle;
    private String categorieLibelle;
    private Long positionId;
    private String positionLibelle;
    private Double latitude;
    private Double longitude;
    private long nbHoraires;
}
