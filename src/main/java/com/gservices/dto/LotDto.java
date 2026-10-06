package com.gservices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LotDto implements Serializable {
    private Long id;
    @NotBlank @Size(max = 60) private String numeroLot;
    @PositiveOrZero private int quantite;
    private BigDecimal prixAchat;
    private LocalDate dateEntree;
    private LocalDate datePeremption;
    private LocalDateTime dateCreation;
    private boolean etat = true;
    private Long stockId;
    @NotNull private Long moisId;
    private String moisLibelle;
    private Integer anneeValeur;
    private boolean perime;
}
