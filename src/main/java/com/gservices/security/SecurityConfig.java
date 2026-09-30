package com.gservices.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sécurité applicative — <strong>Lot 1</strong> : authentification par formulaire
 * (BCrypt), « se souvenir de moi », verrouillage après N échecs
 * ({@link SecuriteEventListener}), et RBAC fin par {@code @PreAuthorize} activé
 * via {@link EnableMethodSecurity}.
 *
 * <p>CSRF désactivé : les vues JSF postent leur propre jeton d'état
 * ({@code jakarta.faces.ViewState}) et le cookie de session est
 * {@code HttpOnly}/{@code SameSite=Lax}. L'intégration CSRF Spring↔JSF est une
 * tâche de durcissement ultérieure.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecuriteProperties.class)
public class SecurityConfig {

    private final SecuriteProperties props;

    public SecurityConfig(SecuriteProperties props) {
        this.props = props;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.xhtml", "/public/**",
                                 "/login.xhtml", "/do-login", "/error").permitAll()
                .requestMatchers("/jakarta.faces.resource/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().permitAll())
            .formLogin(form -> form
                .loginPage("/login.xhtml")
                .loginProcessingUrl("/do-login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler())
                .failureHandler(failureHandler())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/do-logout")
                .logoutSuccessUrl("/login.xhtml?deconnexion=1")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll())
            .rememberMe(rm -> rm
                .key(props.getRememberMeCle())
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds((int) props.getDureeRememberMe().toSeconds()))
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/public/error.xhtml"));
        return http.build();
    }

    /**
     * Distingue « compte en attente de validation » (DisabledException) des
     * autres échecs, pour afficher le bon message sur la page de connexion.
     */
    private org.springframework.security.web.authentication.AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            String cible = exception instanceof org.springframework.security.authentication.DisabledException
                    ? "/login.xhtml?attente=1"
                    : "/login.xhtml?error=1";
            response.sendRedirect(request.getContextPath() + cible);
        };
    }

    /**
     * Redirige après connexion selon le rôle : tableau de bord pour un
     * administrateur, écran Catalogue pour un prestataire, site public pour
     * un simple client — plutôt qu'un mur « 403 ».
     */
    private org.springframework.security.web.authentication.AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            java.util.Set<String> autorites = new java.util.HashSet<>();
            authentication.getAuthorities().forEach(a -> autorites.add(a.getAuthority()));
            // Chaque branche pointe vers une page que l'utilisateur peut réellement
            // ouvrir : un profil avec STOCK_LIRE mais sans CATALOGUE_LIRE (ex.
            // GESTIONNAIRE) ne doit pas être envoyé sur le Catalogue (Accès refusé).
            String cible;
            if (autorites.contains(com.gservices.security.Perms.DASHBOARD_LIRE)) {
                cible = "/admin/index.xhtml";
            } else if (autorites.contains(com.gservices.security.Perms.CATALOGUE_LIRE)) {
                cible = "/admin/cat/catalogue.xhtml";
            } else if (autorites.contains(com.gservices.security.Perms.STOCK_LIRE)) {
                cible = "/admin/stock/stock.xhtml";
            } else if (autorites.contains(com.gservices.security.Perms.AVIS_LIRE)) {
                cible = "/admin/avis/moderation.xhtml";
            } else if (autorites.contains(com.gservices.security.Perms.COMMANDE_LIRE)) {
                cible = "/admin/commandes/commandes.xhtml";
            } else if (autorites.contains(com.gservices.security.Perms.FOURNISSEUR_LIRE)
                    || autorites.contains(com.gservices.security.Perms.CLIENT_LIRE)) {
                cible = "/admin/sec/fournisseurs.xhtml";
            } else {
                cible = "/public/index.xhtml";
            }
            response.sendRedirect(request.getContextPath() + cible);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
