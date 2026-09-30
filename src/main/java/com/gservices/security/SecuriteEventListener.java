package com.gservices.security;

import com.gservices.entity.Personne;
import com.gservices.repository.PersonneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Applique la règle de gestion « verrouillage du compte après N échecs » et
 * trace la dernière connexion.
 *
 * <ul>
 *   <li>échec  → incrémente {@code tentatives_echouees} ; au-delà du seuil,
 *       positionne {@code verrouille_jusqua = now + duree} ;</li>
 *   <li>succès → remet le compteur à zéro, lève le verrou, met à jour
 *       {@code derniere_connexion}.</li>
 * </ul>
 */
@Component
public class SecuriteEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecuriteEventListener.class);

    private final PersonneRepository personneRepository;
    private final SecuriteProperties props;

    public SecuriteEventListener(PersonneRepository personneRepository, SecuriteProperties props) {
        this.personneRepository = personneRepository;
        this.props = props;
    }

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        String login = event.getAuthentication().getName();
        personneRepository.findByLoginIgnoreCase(login).ifPresent(p -> {
            p.setTentativesEchouees(0);
            p.setVerrouilleJusqua(null);
            p.setDerniereConnexion(LocalDateTime.now());
        });
    }

    @EventListener
    @Transactional
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String login = String.valueOf(event.getAuthentication().getName());
        personneRepository.findByLoginIgnoreCase(login).ifPresent(p -> {
            LocalDateTime maintenant = LocalDateTime.now();

            // Compte déjà verrouillé et le verrou court toujours : ne pas prolonger.
            if (p.getVerrouilleJusqua() != null && p.getVerrouilleJusqua().isAfter(maintenant)) {
                return;
            }
            // Verrou expiré : on repart d'un compteur propre.
            if (p.getVerrouilleJusqua() != null) {
                p.setVerrouilleJusqua(null);
                p.setTentativesEchouees(0);
            }

            int tentatives = p.getTentativesEchouees() + 1;
            p.setTentativesEchouees(tentatives);
            if (tentatives >= props.getMaxTentatives()) {
                p.setVerrouilleJusqua(maintenant.plus(props.getDureeVerrouillage()));
                log.warn("Compte '{}' verrouillé jusqu'à {} après {} échecs",
                        login, p.getVerrouilleJusqua(), tentatives);
            }
        });
    }

    /** Utilitaire : déverrouillage manuel (appelé depuis l'écran Personnes). */
    @Transactional
    public void deverrouiller(Personne p) {
        p.setTentativesEchouees(0);
        p.setVerrouilleJusqua(null);
        personneRepository.save(p);
    }
}
