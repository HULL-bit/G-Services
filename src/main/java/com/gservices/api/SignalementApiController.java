package com.gservices.api;

import com.gservices.api.dto.ApiDtos.MessageResponse;
import com.gservices.api.dto.ApiDtos.SignalementRequest;
import com.gservices.entity.MotifSignalement;
import com.gservices.service.SignalementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/** Signalement d'un service depuis l'app mobile (client connecté) — miroir du web. */
@RestController
@RequestMapping("/api")
public class SignalementApiController {

    private final SignalementService signalements;

    public SignalementApiController(SignalementService signalements) {
        this.signalements = signalements;
    }

    /** Motifs disponibles, pour peupler le sélecteur côté app sans les dupliquer en dur. */
    @GetMapping("/signalement-motifs")
    public List<String> motifs() {
        return Arrays.stream(MotifSignalement.values()).map(Enum::name).toList();
    }

    @PostMapping("/services/{id}/signalement")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> signaler(@PathVariable Long id, @Valid @RequestBody SignalementRequest req) {
        signalements.signaler(id, req.motif(), req.description());
        return ResponseEntity.status(201).body(
                new MessageResponse("Signalement envoyé, merci. Un administrateur l'examinera."));
    }
}
