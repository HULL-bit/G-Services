package com.gservices.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Règles de gestion « commande à distance » : total = somme des lignes actives,
 * nombre d'articles = somme des quantités, et cycle de vie des statuts.
 */
class CommandeRegleTest {

    private LigneCommande ligne(int qte, String prix, boolean etat) {
        LigneCommande l = new LigneCommande();
        l.setQuantite(qte);
        l.setPrixUnitaire(new BigDecimal(prix));
        l.setEtat(etat);
        return l;
    }

    @Test
    void total_et_nombre_articles_ignorent_les_lignes_desactivees() {
        Commande c = new Commande();
        c.addLigne(ligne(2, "1500", true));   // 3000
        c.addLigne(ligne(1, "5000", true));   // 5000
        c.addLigne(ligne(4, "999", false));   // ignorée
        c.recalculerTotal();

        assertThat(c.getMontantTotal()).isEqualByComparingTo("8000");
        assertThat(c.getNombreArticles()).isEqualTo(3);
    }

    @Test
    void sous_total_ligne() {
        assertThat(ligne(3, "2500", true).getSousTotal()).isEqualByComparingTo("7500");
    }

    @Test
    void statuts_ouverts_vs_termines() {
        assertThat(StatutCommande.NOUVELLE.estOuverte()).isTrue();
        assertThat(StatutCommande.EXPEDIEE.estOuverte()).isTrue();
        assertThat(StatutCommande.LIVREE.estOuverte()).isFalse();
        assertThat(StatutCommande.ANNULEE.estOuverte()).isFalse();
    }
}
