package com.gservices.security;

import com.gservices.entity.CategorieService;
import com.gservices.entity.Personne;
import com.gservices.entity.Service;
import com.gservices.exception.BusinessException;
import com.gservices.repository.PersonneRepository;
import com.gservices.repository.ServiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Périmètre d'un compte <strong>lié à une catégorie</strong> : fournisseur de
 * services ou gestionnaire.
 *
 * <p>Toute {@link Personne} dont {@code categorieSpecialite} est renseignée est
 * restreinte à cette catégorie — les administrateurs (spécialité nulle) gardent
 * la vue globale. Deux façons d'être restreint, selon le profil :</p>
 * <ul>
 *   <li><strong>PRESTATAIRE</strong> — règle « 1 fournisseur = 1 service » : ne
 *       voit que le service qu'il possède (et ses sous-services) ;</li>
 *   <li><strong>tout autre profil (typiquement GESTIONNAIRE)</strong> — supervise
 *       <em>toute la catégorie</em> : tous les services qui y sont rattachés,
 *       sans en posséder aucun.</li>
 * </ul>
 */
@Component
public class PerimetreFournisseur {

    private final PersonneRepository personneRepo;
    private final ServiceRepository serviceRepo;

    public PerimetreFournisseur(PersonneRepository personneRepo, ServiceRepository serviceRepo) {
        this.personneRepo = personneRepo;
        this.serviceRepo = serviceRepo;
    }

    @Transactional(readOnly = true)
    protected Optional<Personne> personneRestreinte() {
        return SecurityUtils.currentPersonneId()
                .flatMap(personneRepo::findWithSpecialiteById)
                .filter(p -> p.getCategorieSpecialite() != null);
    }

    /** Catégorie de rattachement de l'utilisateur courant, vide s'il a la vue globale. */
    @Transactional(readOnly = true)
    public Optional<CategorieService> categorie() {
        return personneRestreinte().map(Personne::getCategorieSpecialite);
    }

    public Long idCategorie() {
        return categorie().map(CategorieService::getId).orElse(null);
    }

    /** {@code true} si l'utilisateur courant est restreint à une catégorie. */
    public boolean estRestreint() {
        return idCategorie() != null;
    }

    /**
     * Identifiants des services que l'utilisateur courant peut voir / gérer.
     * {@code null} = vue globale (administrateur) — aucune restriction.
     * Fournisseur : le service possédé + ses sous-services. Gestionnaire (ou tout
     * autre profil rattaché à une catégorie sans en posséder de service) : tous
     * les services de sa catégorie.
     */
    @Transactional(readOnly = true)
    public Set<Long> servicesAutorises() {
        Optional<Personne> restreint = personneRestreinte();
        if (restreint.isEmpty()) {
            return null;
        }
        Personne p = restreint.get();
        if (aLeProfil(p, "PRESTATAIRE")) {
            Set<Long> ids = new HashSet<>();
            for (Service racine : serviceRepo.findByProprietaireId(p.getId())) {
                collecter(racine, ids);
            }
            return ids;
        }
        // Gestionnaire (ou assimilé) : supervise toute sa catégorie, pas un seul service.
        return serviceRepo.findByCategorieServiceIdAndEtatTrueOrderByLibelleAsc(p.getCategorieSpecialite().getId())
                .stream().map(Service::getId).collect(Collectors.toSet());
    }

    private static boolean aLeProfil(Personne p, String libelleProfil) {
        return p.getRoles() != null && p.getRoles().stream()
                .anyMatch(r -> r.isEtat() && r.getProfil() != null && r.getProfil().isEtat()
                        && libelleProfil.equalsIgnoreCase(r.getProfil().getLibelle()));
    }

    private void collecter(Service s, Set<Long> acc) {
        if (s == null || !acc.add(s.getId())) {
            return;
        }
        if (s.getSousServices() != null) {
            s.getSousServices().forEach(e -> collecter(e, acc));
        }
    }

    public boolean couvreService(Long idService) {
        Set<Long> autorises = servicesAutorises();
        return autorises == null || (idService != null && autorises.contains(idService));
    }

    /** {@code true} si l'utilisateur peut créer dans cette catégorie (globale ou = sa spécialité). */
    public boolean couvreCategorie(Long idCategorie) {
        Long perim = idCategorie();
        return perim == null || perim.equals(idCategorie);
    }

    public void exigerService(Long idService) {
        if (!couvreService(idService)) {
            throw new BusinessException("Ce service ne fait pas partie de votre périmètre.");
        }
    }

    public void exigerCategorie(Long idCategorie) {
        Optional<CategorieService> c = categorie();
        if (c.isPresent() && !c.get().getId().equals(idCategorie)) {
            throw new BusinessException("Votre espace ne couvre que la catégorie « " + c.get().getLibelle() + " ».");
        }
    }
}
