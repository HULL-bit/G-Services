package com.gservices.mapper;

import com.gservices.dto.ArticleDto;
import com.gservices.dto.PaysDto;
import com.gservices.entity.Article;
import com.gservices.entity.Pays;
import com.gservices.entity.Produit;
import com.gservices.entity.VarieteArticle;
import com.gservices.entity.ZoneGeographique;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Vérifie les mappings non triviaux (calculés / dérivés). Impls MapStruct instanciées directement. */
class CatalogueMapperTest {

    private final ArticleMapper articleMapper = new ArticleMapperImpl();
    private final PaysMapper paysMapper = new PaysMapperImpl();

    @Test
    void article_dto_porte_prix_net_libelle_produit_et_nb_varietes() {
        Produit produit = new Produit();
        produit.setId(7L);
        produit.setLibelle("Chemise Oxford");

        Article a = new Article();
        a.setId(3L);
        a.setReference("CHEM-OXF-BLC-M");
        a.setPrix(new BigDecimal("15000"));
        a.setPromotion(true);
        a.setTauxRemisePourcentage(new BigDecimal("10"));
        a.setProduit(produit);
        a.getVarietes().add(new VarieteArticle());
        a.getVarietes().add(new VarieteArticle());

        ArticleDto dto = articleMapper.toDto(a);

        assertThat(dto.getProduitId()).isEqualTo(7L);
        assertThat(dto.getProduitLibelle()).isEqualTo("Chemise Oxford");
        assertThat(dto.getPrixNet()).isEqualByComparingTo("13500.00");
        assertThat(dto.getNbVarietes()).isEqualTo(2);
    }

    @Test
    void pays_dto_derive_le_drapeau_emoji_de_l_iso2() {
        Pays pays = new Pays();
        pays.setLibelle("Sénégal");
        pays.setCodeIso2("SN");
        ZoneGeographique zone = new ZoneGeographique();
        zone.setId(1L);
        zone.setLibelle("Afrique de l'Ouest");
        pays.setZoneGeographique(zone);

        PaysDto dto = paysMapper.toDto(pays);

        assertThat(dto.getZoneGeographiqueId()).isEqualTo(1L);
        assertThat(dto.getDrapeauEmoji()).isEqualTo("🇸🇳"); // 🇸🇳
    }

    @Test
    void pays_dto_sans_iso2_a_un_drapeau_vide() {
        Pays pays = new Pays();
        pays.setLibelle("Inconnu");
        assertThat(paysMapper.toDto(pays).getDrapeauEmoji()).isEmpty();
    }
}
