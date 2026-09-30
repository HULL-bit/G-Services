package com.gservices.web.bean;

import com.gservices.dto.CommandeDto;
import com.gservices.entity.StatutCommande;
import com.gservices.service.CommandeService;
import com.gservices.web.util.LazyModelService;
import jakarta.annotation.PostConstruct;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Écran back-office de traitement des commandes à distance (branche 7000). */
@Component("commandeBean")
@Scope("view")
public class CommandeAdminBean extends AbstractAdminBean {

    private final transient CommandeService service;

    private String filtre;
    private StatutCommande statut;
    private LazyDataModel<CommandeDto> model;

    private CommandeDto selection = new CommandeDto();

    public CommandeAdminBean(CommandeService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        model = new LazyModelService<>(
                (f, pageable) -> service.rechercherCommandes(f, statut, pageable),
                CommandeDto::getId, () -> filtre);
    }

    public StatutCommande[] getStatuts() {
        return StatutCommande.values();
    }

    public void voir(CommandeDto c) {
        executer(() -> selection = service.commande(c.getId()));
    }

    public void avancer(CommandeDto c, StatutCommande cible) {
        if (executer(() -> service.changerStatut(c.getId(), cible))) {
            info(msg("commande.statut.maj"));
        }
    }

    public void avancerSelection(StatutCommande cible) {
        if (executer(() -> selection = service.changerStatut(selection.getId(), cible))) {
            info(msg("commande.statut.maj"));
            callback("ok", true);
        }
    }

    /** Prochains statuts possibles depuis l'état courant (pour les boutons). */
    public StatutCommande[] getTransitions(CommandeDto c) {
        if (c == null || c.getStatut() == null) {
            return new StatutCommande[0];
        }
        return switch (c.getStatut()) {
            case NOUVELLE -> new StatutCommande[]{StatutCommande.CONFIRMEE, StatutCommande.ANNULEE};
            case CONFIRMEE -> new StatutCommande[]{StatutCommande.EN_PREPARATION, StatutCommande.ANNULEE};
            case EN_PREPARATION -> new StatutCommande[]{StatutCommande.EXPEDIEE, StatutCommande.ANNULEE};
            case EXPEDIEE -> new StatutCommande[]{StatutCommande.LIVREE, StatutCommande.ANNULEE};
            default -> new StatutCommande[0];
        };
    }

    public long getNombreATraiter() {
        try {
            return service.nbCommandesATraiter();
        } catch (RuntimeException e) {
            return 0;
        }
    }

    // --- accesseurs ---
    public String getFiltre() { return filtre; }
    public void setFiltre(String v) { this.filtre = v; }
    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande v) { this.statut = v; }
    public LazyDataModel<CommandeDto> getModel() { return model; }
    public CommandeDto getSelection() { return selection; }
}
