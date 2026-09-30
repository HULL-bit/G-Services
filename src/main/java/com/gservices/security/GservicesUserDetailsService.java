package com.gservices.security;

import com.gservices.repository.PersonneRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Charge une {@link com.gservices.entity.Personne} par son login pour Spring
 * Security. Le graphe profils → permissions est chargé en une requête
 * ({@code @EntityGraph} sur le repository).
 */
@Service
public class GservicesUserDetailsService implements UserDetailsService {

    private final PersonneRepository personneRepository;

    public GservicesUserDetailsService(PersonneRepository personneRepository) {
        this.personneRepository = personneRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return personneRepository.findByLoginIgnoreCase(username)
                .map(GservicesUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Identifiants invalides"));
    }
}
