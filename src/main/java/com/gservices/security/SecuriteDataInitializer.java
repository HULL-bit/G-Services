package com.gservices.security;

import com.gservices.entity.Personne;
import com.gservices.entity.Profil;
import com.gservices.entity.Role;
import com.gservices.entity.Sexe;
import com.gservices.repository.PersonneRepository;
import com.gservices.repository.ProfilRepository;
import com.gservices.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Crée le compte <strong>SUPER_ADMIN</strong> au premier démarrage s'il n'existe
 * pas (le mot de passe ne peut pas être semé en SQL puisqu'il doit être haché
 * BCrypt). Idempotent : ne fait rien si le login est déjà présent.
 *
 * <p>Paramétrable via {@code gservices.securite.bootstrap.*} — cf.
 * {@link SecuriteProperties}. En production, définir un mot de passe fort et le
 * changer après la première connexion.</p>
 */
@Component
@Order(10)
public class SecuriteDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SecuriteDataInitializer.class);
    private static final String PROFIL_SUPER_ADMIN = "SUPER_ADMIN";

    private final PersonneRepository personneRepository;
    private final ProfilRepository profilRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecuriteProperties props;

    public SecuriteDataInitializer(PersonneRepository personneRepository,
                                   ProfilRepository profilRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder,
                                   SecuriteProperties props) {
        this.personneRepository = personneRepository;
        this.profilRepository = profilRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var cfg = props.getBootstrap();
        if (!cfg.isActif()) {
            return;
        }
        if (personneRepository.existsByLoginIgnoreCase(cfg.getLogin())) {
            log.debug("Compte bootstrap '{}' déjà présent — rien à faire", cfg.getLogin());
            return;
        }

        Profil superAdmin = profilRepository.findByLibelleIgnoreCase(PROFIL_SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Profil " + PROFIL_SUPER_ADMIN + " absent : la migration V3 a-t-elle tourné ?"));

        Personne p = new Personne();
        p.setNom(cfg.getNom());
        p.setPrenom(cfg.getPrenom());
        p.setSexe(Sexe.HOMME);
        p.setLogin(cfg.getLogin());
        p.setEmail1(cfg.getEmail());
        p.setPassword(passwordEncoder.encode(cfg.getMotDePasse()));
        p.setEtat(true);
        personneRepository.save(p);

        Role role = new Role();
        role.setPersonne(p);
        role.setProfil(superAdmin);
        role.setDateAttribution(LocalDateTime.now());
        role.setEtat(true);
        roleRepository.save(role);

        log.warn("=== Compte SUPER_ADMIN créé : login='{}' / mot de passe='{}' "
                        + "— À CHANGER APRÈS LA PREMIÈRE CONNEXION ===",
                cfg.getLogin(), cfg.getMotDePasse());
    }
}
