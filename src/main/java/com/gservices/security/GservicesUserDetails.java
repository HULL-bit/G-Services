package com.gservices.security;

import com.gservices.entity.Personne;
import com.gservices.entity.PersonnePermission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Adaptateur Spring Security d'une {@link Personne}.
 *
 * <p>Autorités = codes des {@code Permission} accordées via les profils actifs
 * ({@code Personne → Role → Profil → RoleProfil → Permission}) + une autorité
 * {@code ROLE_<LIBELLE_PROFIL>} par profil actif, <strong>ajustées</strong> par les
 * surcharges par utilisateur ({@link PersonnePermission}, hors UML) : une permission
 * peut être ajoutée ou retirée individuellement sans toucher aux profils.</p>
 */
public class GservicesUserDetails implements UserDetails, Serializable {

    private final Long personneId;
    private final String username;
    private final String password;
    private final String nomComplet;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final Set<GrantedAuthority> authorities;

    public GservicesUserDetails(Personne p) {
        this.personneId = p.getId();
        this.username = p.getLogin();
        this.password = p.getPassword();
        this.nomComplet = p.getNomComplet();
        this.enabled = p.isEtat() && p.isValide();
        this.accountNonLocked = !p.isCompteVerrouille();
        this.authorities = buildAuthorities(p);
    }

    private static Set<GrantedAuthority> buildAuthorities(Personne p) {
        Set<GrantedAuthority> auths = new LinkedHashSet<>();
        if (p.getRoles() == null) {
            return auths;
        }
        p.getRoles().stream()
                .filter(r -> r.isEtat() && r.getProfil() != null && r.getProfil().isEtat())
                .forEach(role -> {
                    var profil = role.getProfil();
                    auths.add(new SimpleGrantedAuthority(
                            "ROLE_" + profil.getLibelle().toUpperCase().replace(' ', '_')));
                    if (profil.getRoleProfils() != null) {
                        profil.getRoleProfils().stream()
                                .filter(rp -> rp.isEtat() && rp.getPermission() != null && rp.getPermission().isEtat())
                                .forEach(rp -> auths.add(
                                        new SimpleGrantedAuthority(rp.getPermission().getCode())));
                    }
                });

        if (p.getPermissionsDirectes() != null) {
            for (PersonnePermission pp : p.getPermissionsDirectes()) {
                if (!pp.isEtat() || pp.getPermission() == null || !pp.getPermission().isEtat()) {
                    continue;
                }
                String code = pp.getPermission().getCode();
                if (pp.isAccordee()) {
                    auths.add(new SimpleGrantedAuthority(code));
                } else {
                    auths.removeIf(a -> a.getAuthority().equals(code));
                }
            }
        }
        return auths;
    }

    public Long getPersonneId()   { return personneId; }
    public String getNomComplet() { return nomComplet; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword()  { return password; }
    @Override public String getUsername()  { return username; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return accountNonLocked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return enabled; }
}
