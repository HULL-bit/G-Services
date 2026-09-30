package com.gservices.web.bean;

import com.gservices.security.Perms;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Rend les constantes de {@link Perms} accessibles à l'EL :
 * {@code #{utilisateurCourant.autorise(perms.PERSONNE_CREER)}}.
 */
@Component("perms")
public class PermsBean extends HashMap<String, String> implements Serializable {

    @PostConstruct
    void init() {
        for (Field f : Perms.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                try {
                    put(f.getName(), (String) f.get(null));
                } catch (IllegalAccessException ignored) {
                    // constantes publiques : n'arrive pas
                }
            }
        }
    }
}
