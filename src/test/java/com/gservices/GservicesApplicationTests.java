package com.gservices;

import com.gservices.entity.Stock;
import com.gservices.repository.AvisRepository;
import com.gservices.repository.PersonneRepository;
import com.gservices.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration de bout en bout sur une vraie base PostgreSQL 16
 * (Testcontainers) : le contexte Spring démarre, Flyway applique <strong>toutes</strong>
 * les migrations, l'IHM JSF/PrimeFaces s'initialise, et les jeux de démonstration
 * ainsi que les règles de gestion sont vérifiés.
 *
 * <p>Nécessite un daemon exposant l'API Docker ≥ 1.40. Sous Podman :
 * {@code export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock}.</p>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GservicesApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    PersonneRepository personneRepository;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    AvisRepository avisRepository;

    @Test
    void contexte_demarre_et_migrations_appliquees() {
        // Le compte d'amorçage est créé au démarrage.
        assertThat(personneRepository.findByLoginIgnoreCase("superadmin")).isPresent();
    }

    @Test
    void seed_de_demonstration_present() {
        // V19 : la chemise Oxford et ses déclinaisons stockées.
        assertThat(stockRepository.count()).isGreaterThanOrEqualTo(8);
        assertThat(avisRepository.count()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void regle_stock_le_disponible_exclut_les_lots_perimes() {
        Stock bis33 = stockRepository.findByEtatTrue().stream()
                .filter(s -> "BIS-33".equalsIgnoreCase(s.getArticle().getReference()))
                .findFirst().orElseThrow();
        // Un lot périmé est présent dans la démo mais ne compte pas.
        assertThat(bis33.getLots()).anyMatch(com.gservices.entity.Lot::isPerime);
        assertThat(bis33.getQuantiteDisponible())
                .isLessThan(bis33.getLots().stream().mapToInt(com.gservices.entity.Lot::getQuantite).sum());
    }

    @Test
    void regle_avis_seuls_les_avis_moderes_sont_publics() {
        long moderes = avisRepository.findAll().stream().filter(a -> a.isEstModere() && a.isEtat()).count();
        long publicsService = avisRepository.findAll().stream()
                .filter(a -> a.getService() != null && a.isPublic()).count();
        assertThat(moderes).isPositive();
        assertThat(avisRepository.findAll().stream().anyMatch(a -> !a.isEstModere())).isTrue();
        assertThat(publicsService).isLessThanOrEqualTo(moderes);
    }
}
