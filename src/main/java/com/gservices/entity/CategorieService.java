package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/** {@code CategorieService} — regroupe des {@link Service} (paquetage GestServices). */
@Entity
@Table(name = "categorie_service", indexes = {
        @Index(name = "idx_categorie_service_libelle", columnList = "libelle"),
        @Index(name = "idx_categorie_service_ordre", columnList = "ordre_affichage")
})
@Getter
@Setter
public class CategorieService extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie_service")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Size(max = 60)
    @Column(name = "icone", length = 60)
    private String icone;

    @Column(name = "ordre_affichage", nullable = false)
    private int ordreAffichage = 0;

    @OneToMany(mappedBy = "categorieService")
    private Set<Service> services = new LinkedHashSet<>();
}
