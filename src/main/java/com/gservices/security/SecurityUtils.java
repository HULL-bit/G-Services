package com.gservices.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Accès utilitaire à l'utilisateur authentifié courant. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<GservicesUserDetails> currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && a.getPrincipal() instanceof GservicesUserDetails u) {
            return Optional.of(u);
        }
        return Optional.empty();
    }

    public static Optional<Long> currentPersonneId() {
        return currentUser().map(GservicesUserDetails::getPersonneId);
    }

    public static Optional<String> currentLogin() {
        return currentUser().map(GservicesUserDetails::getUsername);
    }

    public static boolean isAuthenticated() {
        return currentUser().isPresent();
    }

    public static Set<String> currentAuthorities() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) {
            return Set.of();
        }
        return a.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public static boolean hasPermission(String code) {
        return currentAuthorities().contains(code);
    }
}
