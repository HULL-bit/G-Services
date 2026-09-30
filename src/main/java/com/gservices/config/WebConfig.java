package com.gservices.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Configuration Web transverse : redirection de la racine vers l'accueil JSF,
 * source de messages i18n partagée (Spring + JSF pointent sur le même bundle).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** {@code GET /} → page d'accueil publique rendue par la {@code FacesServlet}. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/public/index.xhtml");
        registry.addRedirectViewController("/index.xhtml", "/public/index.xhtml");
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:i18n/messages");
        ms.setDefaultEncoding(StandardCharsets.UTF_8.name());
        ms.setDefaultLocale(Locale.FRENCH);
        ms.setFallbackToSystemLocale(false);
        ms.setCacheSeconds(30);
        return ms;
    }
}
