package com.gservices.security;

import com.gservices.entity.Permission;
import com.gservices.entity.Personne;
import com.gservices.entity.PersonnePermission;
import com.gservices.entity.Profil;
import com.gservices.entity.Role;
import com.gservices.entity.RoleProfil;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dérivation des autorités RBAC : {@code Personne → Role → Profil → RoleProfil →
 * Permission}. Aucune base requise.
 */
class GservicesUserDetailsTest {

    private Permission permission(String code, boolean actif) {
        Permission p = new Permission();
        p.setId((long) code.hashCode());
        p.setCode(code);
        p.setLibelle(code);
        p.setEtat(actif);
        return p;
    }

    private Personne personneAvecProfil(String libelleProfil, boolean profilActif,
                                        boolean roleActif, Permission... permissions) {
        Personne personne = new Personne();
        personne.setId(1L);
        personne.setLogin("jdoe");
        personne.setNom("Doe");
        personne.setPrenom("Jane");
        personne.setPassword("{bcrypt}x");
        personne.setEtat(true);

        Profil profil = new Profil();
        profil.setId(10L);
        profil.setLibelle(libelleProfil);
        profil.setEtat(profilActif);
        for (Permission perm : permissions) {
            RoleProfil rp = new RoleProfil();
            rp.setId((long) perm.getCode().hashCode());
            rp.setProfil(profil);
            rp.setPermission(perm);
            rp.setEtat(true);
            profil.getRoleProfils().add(rp);
        }

        Role role = new Role();
        role.setId(100L);
        role.setPersonne(personne);
        role.setProfil(profil);
        role.setEtat(roleActif);
        personne.getRoles().add(role);
        return personne;
    }

    @Test
    void construit_les_autorites_a_partir_des_permissions_actives() {
        Personne p = personneAvecProfil("ADMIN", true, true,
                permission("PERSONNE_LIRE", true),
                permission("PERSONNE_CREER", true),
                permission("PERMISSION_OBSOLETE", false));   // permission désactivée -> exclue

        var details = new GservicesUserDetails(p);
        Set<String> codes = SecurityUtilsTestHelper.authorities(details);

        assertThat(codes).contains("PERSONNE_LIRE", "PERSONNE_CREER", "ROLE_ADMIN");
        assertThat(codes).doesNotContain("PERMISSION_OBSOLETE");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    void ignore_les_roles_et_profils_desactives() {
        Personne roleInactif = personneAvecProfil("ADMIN", true, false,
                permission("PERSONNE_LIRE", true));
        assertThat(SecurityUtilsTestHelper.authorities(new GservicesUserDetails(roleInactif))).isEmpty();

        Personne profilInactif = personneAvecProfil("ADMIN", false, true,
                permission("PERSONNE_LIRE", true));
        assertThat(SecurityUtilsTestHelper.authorities(new GservicesUserDetails(profilInactif))).isEmpty();
    }

    @Test
    void compte_verrouille_est_signale() {
        Personne p = personneAvecProfil("ADMIN", true, true, permission("X", true));
        p.setVerrouilleJusqua(java.time.LocalDateTime.now().plusMinutes(10));
        assertThat(new GservicesUserDetails(p).isAccountNonLocked()).isFalse();
    }

    // ---- Surcharges de permission par utilisateur (PersonnePermission) ----

    private void surcharge(Personne p, String code, boolean accordee) {
        PersonnePermission pp = new PersonnePermission();
        pp.setId((long) (code.hashCode() + (accordee ? 1 : 0)));
        pp.setPersonne(p);
        pp.setPermission(permission(code, true));
        pp.setAccordee(accordee);
        pp.setEtat(true);
        p.getPermissionsDirectes().add(pp);
    }

    @Test
    void une_surcharge_accordee_ajoute_une_permission_absente_des_profils() {
        Personne p = personneAvecProfil("AGENT", true, true, permission("PERSONNE_LIRE", true));
        surcharge(p, "GEO_MODIFIER", true);

        assertThat(SecurityUtilsTestHelper.authorities(new GservicesUserDetails(p)))
                .contains("PERSONNE_LIRE", "GEO_MODIFIER");
    }

    @Test
    void une_surcharge_refusee_retire_une_permission_heritee_du_profil() {
        Personne p = personneAvecProfil("AGENT", true, true,
                permission("PERSONNE_LIRE", true),
                permission("PERSONNE_SUPPRIMER", true));
        surcharge(p, "PERSONNE_SUPPRIMER", false);

        var codes = SecurityUtilsTestHelper.authorities(new GservicesUserDetails(p));
        assertThat(codes).contains("PERSONNE_LIRE");
        assertThat(codes).doesNotContain("PERSONNE_SUPPRIMER");
    }

    @Test
    void une_surcharge_desactivee_est_ignoree() {
        Personne p = personneAvecProfil("AGENT", true, true, permission("PERSONNE_LIRE", true));
        PersonnePermission pp = new PersonnePermission();
        pp.setId(999L);
        pp.setPersonne(p);
        pp.setPermission(permission("GEO_MODIFIER", true));
        pp.setAccordee(true);
        pp.setEtat(false);   // surcharge inactive
        p.getPermissionsDirectes().add(pp);

        assertThat(SecurityUtilsTestHelper.authorities(new GservicesUserDetails(p)))
                .doesNotContain("GEO_MODIFIER");
    }
}
