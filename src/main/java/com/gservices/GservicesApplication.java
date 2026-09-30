package com.gservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Point d'entrée de la plateforme <strong>G-SERVICES</strong> — gestion de services
 * &amp; produits géolocalisés.
 *
 * <p>Stack : Spring Boot 3.3 · Jakarta Faces (Mojarra) + PrimeFaces 14 via JoinFaces ·
 * Spring Data JPA / Hibernate · PostgreSQL 16 · Flyway · Spring Security.</p>
 *
 * <p>Le WAR produit est <em>exécutable</em> ({@code java -jar gservices.war}) grâce au
 * conteneur Tomcat embarqué — aucun serveur d'application externe n'est requis.</p>
 */
@SpringBootApplication
@EnableCaching
public class GservicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GservicesApplication.class, args);
    }
}
