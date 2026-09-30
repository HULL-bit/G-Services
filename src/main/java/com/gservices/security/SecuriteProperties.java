package com.gservices.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Paramètres de sécurité externalisés (préfixe {@code gservices.securite}).
 * Valeurs par défaut adaptées au développement — à durcir via variables
 * d'environnement en production.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "gservices.securite")
public class SecuriteProperties {

    /** Nombre d'échecs de connexion consécutifs avant verrouillage du compte. */
    private int maxTentatives = 5;

    /** Durée de verrouillage du compte après dépassement du seuil. */
    private Duration dureeVerrouillage = Duration.ofMinutes(15);

    /** Validité du jeton « se souvenir de moi ». */
    private Duration dureeRememberMe = Duration.ofDays(14);

    /** Clé de signature des jetons remember-me (à surcharger en production !). */
    private String rememberMeCle = "gservices-remember-me-CHANGER-EN-PROD";

    private final Bootstrap bootstrap = new Bootstrap();

    /** Compte SUPER_ADMIN créé au premier démarrage s'il n'existe pas. */
    @Getter
    @Setter
    public static class Bootstrap {
        private boolean actif = true;
        private String login = "superadmin";
        private String motDePasse = "Admin@2026";
        private String nom = "Super";
        private String prenom = "Admin";
        private String email = "superadmin@gservices.com";
    }
}
