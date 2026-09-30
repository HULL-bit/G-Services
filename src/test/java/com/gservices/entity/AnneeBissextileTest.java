package com.gservices.entity;

import org.junit.jupiter.api.Test;

import java.time.Month;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Règle §10 : années bissextiles et nombre de jours de février.
 * (Reflète le calcul de {@code TempsServiceImpl.creerAnnee}.)
 */
class AnneeBissextileTest {

    @Test
    void detection_annee_bissextile() {
        assertThat(Year.isLeap(2024)).isTrue();
        assertThat(Year.isLeap(2000)).isTrue();   // divisible par 400
        assertThat(Year.isLeap(2025)).isFalse();
        assertThat(Year.isLeap(2026)).isFalse();
        assertThat(Year.isLeap(1900)).isFalse();  // divisible par 100 mais pas 400
    }

    @Test
    void fevrier_a_28_ou_29_jours() {
        assertThat(Month.FEBRUARY.length(true)).isEqualTo(29);
        assertThat(Month.FEBRUARY.length(false)).isEqualTo(28);
    }
}
