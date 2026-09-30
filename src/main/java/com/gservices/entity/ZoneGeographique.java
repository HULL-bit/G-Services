package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/** {@code ZoneGeographique} — regroupement de pays au sein d'un {@link Continent}. */
@Entity
@Table(name = "zone_geographique", indexes = {
        @Index(name = "idx_zone_code", columnList = "code", unique = true),
        @Index(name = "idx_zone_continent", columnList = "id_continent")
})
@Getter
@Setter
public class ZoneGeographique extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zone_geographique")
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "libelle", nullable = false, length = 80)
    private String libelle;

    @Size(max = 10)
    @Column(name = "code", unique = true, length = 10)
    private String code;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_continent", nullable = false,
            foreignKey = @ForeignKey(name = "fk_zone_continent"))
    private Continent continent;

    @OneToMany(mappedBy = "zoneGeographique")
    private Set<Pays> pays = new LinkedHashSet<>();
}
