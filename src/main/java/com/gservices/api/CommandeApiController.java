package com.gservices.api;

import com.gservices.api.dto.ApiDtos.CommandeRequest;
import com.gservices.dto.ArticleDto;
import com.gservices.dto.CommandeDto;
import com.gservices.dto.LigneCommandeDto;
import com.gservices.service.CommandeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/** Commande à distance depuis l'app mobile (client connecté). */
@RestController
@RequestMapping("/api")
public class CommandeApiController {

    private final CommandeService commandes;

    public CommandeApiController(CommandeService commandes) {
        this.commandes = commandes;
    }

    @GetMapping("/services/{id}/articles-commandables")
    public List<ArticleDto> articlesCommandables(@PathVariable Long id) {
        return commandes.articlesCommandables(id);
    }

    @PostMapping("/services/{id}/commande")
    @PreAuthorize("isAuthenticated()")
    public CommandeDto passer(@PathVariable Long id, @Valid @RequestBody CommandeRequest req) {
        CommandeDto dto = new CommandeDto();
        dto.setServiceId(id);
        dto.setAdresseLivraison(req.adresseLivraison());
        dto.setTelephoneContact(req.telephoneContact());
        dto.setCommentaire(req.commentaire());
        List<LigneCommandeDto> lignes = new ArrayList<>();
        if (req.lignes() != null) {
            req.lignes().stream()
                    .filter(l -> l.articleId() != null && l.quantite() > 0)
                    .forEach(l -> {
                        LigneCommandeDto ligne = new LigneCommandeDto();
                        ligne.setArticleId(l.articleId());
                        ligne.setQuantite(l.quantite());
                        lignes.add(ligne);
                    });
        }
        dto.setLignes(lignes);
        return commandes.passerCommande(dto);
    }

    @GetMapping("/me/commandes")
    @PreAuthorize("isAuthenticated()")
    public List<CommandeDto> mesCommandes() {
        return commandes.mesCommandes();
    }

    @GetMapping("/me/commandes/{id}")
    @PreAuthorize("isAuthenticated()")
    public CommandeDto maCommande(@PathVariable Long id) {
        return commandes.maCommande(id);
    }
}
