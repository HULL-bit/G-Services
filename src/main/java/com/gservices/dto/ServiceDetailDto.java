package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Fiche publique complète d'un service. */
@Data
public class ServiceDetailDto implements Serializable {
    private ServiceVitrineDto service;
    private InformationServiceDto prestataire;
    private List<HoraireDto> horaires = new ArrayList<>();
    private List<CatalogueVitrineDto> catalogues = new ArrayList<>();
    private SyntheseAvisDto synthese;
    private List<AvisDto> avis = new ArrayList<>();
}
