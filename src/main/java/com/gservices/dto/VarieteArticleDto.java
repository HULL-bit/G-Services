package com.gservices.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class VarieteArticleDto implements Serializable {
    private Long id;
    @Size(max = 255) private String valeur;
    private boolean etat = true;
    @NotNull private Long articleId;
    private String articleReference;
    @NotNull private Long proprietesArticleId;
    private String proprietesArticleLibelle;
    private String typeSaisie;
    private String uniteMesureSymbole;
}
