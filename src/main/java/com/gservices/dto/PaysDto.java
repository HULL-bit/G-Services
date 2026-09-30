package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class PaysDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 100) private String libelle;
    @Size(max = 2) private String codeIso2;
    @Size(max = 3) private String codeIso3;
    @Size(max = 10) private String indicatifTelephonique;
    @Size(max = 10) private String devise;
    @Size(max = 100) private String capitale;
    @Size(max = 80) private String langueOfficielle;
    private Double superficieKm2;
    private Long population;
    @Size(max = 120) private String drapeau;
    private boolean etat = true;
    @NotNull private Long zoneGeographiqueId;
    private String zoneGeographiqueLibelle;
    private String continentLibelle;
    private long nbRegions;

    /** Drapeau emoji dérivé du code ISO-2 (aucun asset requis). */
    public String getDrapeauEmoji() {
        if (codeIso2 == null || codeIso2.length() != 2) {
            return "";
        }
        String cc = codeIso2.toUpperCase();
        int a = 0x1F1E6 + (cc.charAt(0) - 'A');
        int b = 0x1F1E6 + (cc.charAt(1) - 'A');
        return new String(Character.toChars(a)) + new String(Character.toChars(b));
    }
}
