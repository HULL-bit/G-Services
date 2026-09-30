package com.gservices;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Permet le déploiement du WAR dans un conteneur de servlets externe
 * (Tomcat, WildFly…). Le mode privilégié reste toutefois le WAR exécutable
 * lancé par {@code java -jar} (voir {@link GservicesApplication}).
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(GservicesApplication.class);
    }
}
