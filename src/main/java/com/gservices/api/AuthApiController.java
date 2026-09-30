package com.gservices.api;

import com.gservices.api.dto.ApiDtos.*;
import com.gservices.dto.InscriptionDto;
import com.gservices.security.GservicesUserDetails;
import com.gservices.security.SecurityUtils;
import com.gservices.service.PersonneService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Authentification de l'app mobile : login (JWT), inscription, profil courant. */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final PersonneService personneService;

    public AuthApiController(AuthenticationManager authManager, JwtService jwt, PersonneService personneService) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.personneService = personneService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.login(), req.motDePasse()));
        GservicesUserDetails u = (GservicesUserDetails) auth.getPrincipal();
        String token = jwt.generer(u.getUsername());
        return new AuthResponse(token, jwt.ttlSecondes(), toUserInfo(u));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest req) {
        InscriptionDto dto = new InscriptionDto();
        dto.setTypeCompte(req.typeCompte());
        dto.setNom(req.nom());
        dto.setPrenom(req.prenom());
        dto.setEmail(req.email());
        dto.setLogin(req.login());
        dto.setMotDePasse(req.motDePasse());
        dto.setMotDePasseConfirme(req.motDePasse());
        dto.setCategorieSpecialiteId(req.categorieSpecialiteId());
        dto.setServiceLibelle(req.serviceLibelle());
        personneService.inscrire(dto);
        return ResponseEntity.status(201).body(new MessageResponse(
                "Compte créé. Il doit être validé par un administrateur avant la première connexion."));
    }

    @GetMapping("/me")
    public UserInfo me() {
        return SecurityUtils.currentUser().map(AuthApiController::toUserInfo)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Non authentifié"));
    }

    private static UserInfo toUserInfo(GservicesUserDetails u) {
        List<String> profils = u.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .toList();
        return new UserInfo(u.getPersonneId(), u.getUsername(), u.getNomComplet(), profils);
    }
}
