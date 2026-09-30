package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CatalogueVitrineDto implements Serializable {
    private Long id;
    private String libelle;
    private String description;
    private String icone;
    private List<ProduitVitrineDto> produits = new ArrayList<>();
}
