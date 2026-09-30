package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code InformationService} — fiche « prestataire » d'un {@link Service}
 * (coordonnées, disponibilité). Optionnellement géolocalisée par une
 * {@link Position} ; planifie ses {@link Horaire}s.
 */
@Entity
@Table(name = "information_service", indexes = {
        @Index(name = "idx_info_service_service", columnList = "id_service", unique = true),
        @Index(name = "idx_info_service_position", columnList = "id_position")
})
@Getter
@Setter
public class InformationService extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_information_service")
    private Long id;

    @Size(max = 255)
    @Column(name = "adresse")
    private String adresse;

    @Size(max = 30)
    @Column(name = "telephone1", length = 30)
    private String telephone1;

    @Size(max = 30)
    @Column(name = "telephone2", length = 30)
    private String telephone2;

    @Email
    @Size(max = 120)
    @Column(name = "email1", length = 120)
    private String email1;

    @Email
    @Size(max = 120)
    @Column(name = "email2", length = 120)
    private String email2;

    @Size(max = 160)
    @Column(name = "site_web", length = 160)
    private String siteWeb;

    @Column(name = "disponibilite", nullable = false)
    private boolean disponibilite = true;

    @CreationTimestamp
    @Column(name = "date", nullable = false, updatable = false)
    private LocalDateTime date;

    @UpdateTimestamp
    @Column(name = "date_maj")
    private LocalDateTime dateMaj;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_info_service_service"))
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_position",
            foreignKey = @ForeignKey(name = "fk_info_service_position"))
    private Position position;

    @OneToMany(mappedBy = "informationService", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Horaire> horaires = new LinkedHashSet<>();

    public void addHoraire(Horaire h) {
        horaires.add(h);
        h.setInformationService(this);
    }
}
