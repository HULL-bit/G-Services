package com.gservices.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code Stock} — état de disponibilité d'un {@link Article} (relation 1-1 :
 * un article a au plus un stock). Composé de {@link Lot}s ; la quantité
 * réellement disponible ignore les lots périmés ou désactivés.
 */
@Entity
@Table(name = "stock", indexes = @Index(name = "idx_stock_article", columnList = "id_article", unique = true))
@Getter
@Setter
public class Stock extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private Long id;

    /** Quantité de référence saisie (les lots restent la source de vérité du disponible). */
    @Column(name = "quantite_totale", nullable = false)
    private int quantiteTotale = 0;

    @Column(name = "seuil_alerte", nullable = false)
    private int seuilAlerte = 0;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_article", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_stock_article"))
    private Article article;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Lot> lots = new LinkedHashSet<>();

    public void addLot(Lot lot) {
        lots.add(lot);
        lot.setStock(this);
    }

    /** Somme des quantités des lots actifs et non périmés. */
    @Transient
    public int getQuantiteDisponible() {
        LocalDate today = LocalDate.now();
        return lots.stream()
                .filter(Lot::isEtat)
                .filter(l -> l.getDatePeremption() == null || !l.getDatePeremption().isBefore(today))
                .mapToInt(Lot::getQuantite)
                .sum();
    }

    @Transient
    public boolean isEnAlerte() {
        return getQuantiteDisponible() <= seuilAlerte;
    }
}
