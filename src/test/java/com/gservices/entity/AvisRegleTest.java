package com.gservices.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Règle §10 : un avis n'est public qu'une fois modéré ET actif. */
class AvisRegleTest {

    private Avis avis(boolean modere, boolean actif) {
        Avis a = new Avis();
        a.setEstModere(modere);
        a.setEtat(actif);
        return a;
    }

    @Test
    void avis_public_uniquement_si_modere_et_actif() {
        assertThat(avis(true, true).isPublic()).isTrue();
        assertThat(avis(false, true).isPublic()).isFalse();
        assertThat(avis(true, false).isPublic()).isFalse();
        assertThat(avis(false, false).isPublic()).isFalse();
    }
}
