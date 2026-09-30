package com.gservices.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Objects;

/**
 * Racine technique commune à toutes les entités JPA du modèle G-SERVICES.
 *
 * <p>Porte uniquement l'identifiant et le drapeau d'activation {@code etat}
 * (soft-delete / activation — <em>jamais</em> de suppression physique par défaut,
 * cf. conventions BDD). Les colonnes temporelles portent des noms spécifiques
 * dans le modèle UML ({@code date}, {@code dateMAJ}, {@code dateAttribution}…) et
 * sont donc déclarées entité par entité.</p>
 *
 * @param <ID> type de la clé primaire
 */
@MappedSuperclass
public abstract class AbstractEntity<ID extends Serializable> implements Serializable {

    /** Identité fonctionnelle : {@code true} = actif/visible, {@code false} = désactivé. */
    @Column(name = "etat", nullable = false)
    private boolean etat = true;

    public abstract ID getId();

    public boolean isEtat() {
        return etat;
    }

    public void setEtat(boolean etat) {
        this.etat = etat;
    }

    /**
     * Égalité basée sur l'identifiant persistant : deux entités du même type
     * ayant le même id non nul sont égales ; sinon on retombe sur l'identité
     * d'instance (deux entités transiantes ne sont jamais égales).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractEntity<?> other)) {
            return false;
        }
        ID id = getId();
        return id != null
                && getClass().isInstance(o)
                && o.getClass().isInstance(this)
                && Objects.equals(id, other.getId());
    }

    /** Constant : évite les fuites de bucket quand l'id est attribué après persistance. */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + getId();
    }
}
