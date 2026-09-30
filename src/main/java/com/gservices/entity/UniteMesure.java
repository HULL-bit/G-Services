package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** {@code UniteMesure} — unité dans laquelle une {@link ProprietesArticle} est mesurée. */
@Entity
@Table(name = "unite_mesure", indexes = {
        @Index(name = "idx_unite_mesure_symbole", columnList = "symbole", unique = true)
})
@Getter
@Setter
public class UniteMesure extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unite_mesure")
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(name = "libelle", nullable = false, length = 60)
    private String libelle;

    @Size(max = 12)
    @Column(name = "symbole", unique = true, length = 12)
    private String symbole;
}
