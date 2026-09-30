package com.gservices.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code ProprietesArticle} — caractéristique paramétrable d'un article
 * (ex. « Poids », « Couleur », « Taille »). {@code typeSaisie} décrit le mode
 * de saisie attendu (TEXTE, NOMBRE, LISTE, BOOLEEN…).
 *
 * <p>Le lien vers {@link UniteMesure} est optionnel (une couleur n'a pas d'unité) —
 * léger écart assumé au « 1,1 » du diagramme, pour la souplesse du référentiel.</p>
 */
@Entity
@Table(name = "proprietes_article", indexes = {
        @Index(name = "idx_proprietes_article_libelle", columnList = "libelle"),
        @Index(name = "idx_proprietes_article_unite", columnList = "id_unite_mesure")
})
@Getter
@Setter
public class ProprietesArticle extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proprietes_article")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Size(max = 20)
    @Column(name = "type_saisie", length = 20)
    private String typeSaisie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unite_mesure",
            foreignKey = @ForeignKey(name = "fk_proprietes_article_unite"))
    private UniteMesure uniteMesure;

    @OneToMany(mappedBy = "proprietesArticle")
    private Set<VarieteArticle> varietes = new LinkedHashSet<>();

    /** Produits qui déclarent utiliser cette propriété (association « définit »). */
    @ManyToMany(mappedBy = "proprietes")
    private Set<Produit> produits = new LinkedHashSet<>();
}
