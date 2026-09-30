package com.gservices.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Règle de gestion §10 : prix remisé quand {@code promotion} et taux &gt; 0. */
class ArticleRegleTest {

    private Article article(BigDecimal prix, boolean promo, BigDecimal taux) {
        Article a = new Article();
        a.setPrix(prix);
        a.setPromotion(promo);
        a.setTauxRemisePourcentage(taux);
        return a;
    }

    @Test
    void prix_net_egal_prix_sans_promotion() {
        assertThat(article(new BigDecimal("10000"), false, new BigDecimal("20")).getPrixNet())
                .isEqualByComparingTo("10000");
    }

    @Test
    void prix_net_applique_la_remise_avec_promotion() {
        assertThat(article(new BigDecimal("10000"), true, new BigDecimal("15")).getPrixNet())
                .isEqualByComparingTo("8500.00");
    }

    @Test
    void promotion_sans_taux_ne_change_rien() {
        assertThat(article(new BigDecimal("10000"), true, BigDecimal.ZERO).getPrixNet())
                .isEqualByComparingTo("10000");
        assertThat(article(new BigDecimal("10000"), true, null).getPrixNet())
                .isEqualByComparingTo("10000");
    }

    @Test
    void arrondi_au_centime() {
        assertThat(article(new BigDecimal("999"), true, new BigDecimal("33.33")).getPrixNet())
                .isEqualByComparingTo("666.03");
    }
}
