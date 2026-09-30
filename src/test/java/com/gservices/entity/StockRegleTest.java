package com.gservices.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Règles de gestion §10 : la quantité disponible exclut les lots périmés ou
 * désactivés ; l'alerte se déclenche quand disponible ≤ seuil.
 */
class StockRegleTest {

    private Lot lot(int qte, LocalDate peremption, boolean etat) {
        Lot l = new Lot();
        l.setQuantite(qte);
        l.setDatePeremption(peremption);
        l.setEtat(etat);
        return l;
    }

    private Stock stock(int seuil, Lot... lots) {
        Stock s = new Stock();
        s.setSeuilAlerte(seuil);
        for (Lot l : lots) {
            s.addLot(l);
        }
        return s;
    }

    @Test
    void quantite_disponible_somme_les_lots_valides() {
        Stock s = stock(10,
                lot(30, LocalDate.now().plusMonths(6), true),
                lot(20, null, true));
        assertThat(s.getQuantiteDisponible()).isEqualTo(50);
    }

    @Test
    void quantite_disponible_exclut_les_lots_perimes_et_desactives() {
        Stock s = stock(10,
                lot(30, LocalDate.now().plusMonths(6), true),
                lot(100, LocalDate.now().minusDays(1), true),   // périmé
                lot(50, null, false));                          // désactivé
        assertThat(s.getQuantiteDisponible()).isEqualTo(30);
    }

    @Test
    void alerte_quand_disponible_atteint_le_seuil() {
        assertThat(stock(10, lot(10, null, true)).isEnAlerte()).isTrue();
        assertThat(stock(10, lot(11, null, true)).isEnAlerte()).isFalse();
        assertThat(stock(5).isEnAlerte()).isTrue();  // rupture totale
    }

    @Test
    void un_lot_du_jour_meme_n_est_pas_perime() {
        Lot l = lot(5, LocalDate.now(), true);
        assertThat(l.isPerime()).isFalse();
        assertThat(stock(0, l).getQuantiteDisponible()).isEqualTo(5);
    }
}
