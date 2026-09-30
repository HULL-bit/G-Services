package com.gservices.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Une ligne de l'écran « Permissions effectives » d'une personne : la
 * permission, si elle lui est effectivement accordée, et d'où ça vient
 * (héritée d'un profil, ajoutée ou retirée explicitement pour cette personne).
 */
@Getter
@Setter
@AllArgsConstructor
public class PermissionEffectiveDto implements Serializable {

    /** Origine d'un droit effectif — voir {@link com.gservices.entity.PersonnePermission}. */
    public enum Source { PROFIL, DIRECTE_ACCORDEE, DIRECTE_RETIREE, AUCUNE }

    private Long permissionId;
    private String code;
    private String libelle;
    private String actionAutorisee;
    private String brancheLibelle;
    private boolean issueDuProfil;
    private Source source;

    /** État effectif (ce que la case à cocher doit refléter au départ). */
    public boolean isEffective() {
        return source == Source.PROFIL || source == Source.DIRECTE_ACCORDEE;
    }

    /** Nom de l'énumération en {@code String}, pratique pour l'EL (clé i18n, comparaisons). */
    public String getSourceCode() {
        return source == null ? "AUCUNE" : source.name();
    }
}
