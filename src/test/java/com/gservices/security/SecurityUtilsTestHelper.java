package com.gservices.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.stream.Collectors;

/** Petit utilitaire de test : extrait les codes d'autorité d'un {@link UserDetails}. */
final class SecurityUtilsTestHelper {

    private SecurityUtilsTestHelper() {
    }

    static Set<String> authorities(UserDetails details) {
        return details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
