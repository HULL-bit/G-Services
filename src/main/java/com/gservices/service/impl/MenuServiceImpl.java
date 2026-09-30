package com.gservices.service.impl;

import com.gservices.dto.EntreeMenuDto;
import com.gservices.dto.SectionMenuDto;
import com.gservices.entity.Permission;
import com.gservices.repository.BranchePermissionRepository;
import com.gservices.security.SecurityUtils;
import com.gservices.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MenuServiceImpl implements MenuService {

    private final BranchePermissionRepository brancheRepository;

    public MenuServiceImpl(BranchePermissionRepository brancheRepository) {
        this.brancheRepository = brancheRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionMenuDto> menuCourant() {
        Set<String> autorites = SecurityUtils.currentAuthorities();

        return brancheRepository.findByEtatTrueOrderByNiveauAsc().stream()
                .map(branche -> {
                    // Une entrée par URL distincte (plusieurs permissions LIRE peuvent
                    // pointer vers la même page consolidée).
                    Map<String, Permission> parUrl = new LinkedHashMap<>();
                    branche.getPermissions().stream()
                            .filter(Permission::isEtat)
                            .filter(p -> "LIRE".equalsIgnoreCase(p.getActionAutorisee()))
                            .filter(p -> StringUtils.hasText(p.getReference()))
                            .filter(p -> autorites.contains(p.getCode()))
                            .sorted(Comparator.comparingInt(Permission::getNiveau))
                            .forEach(p -> parUrl.putIfAbsent(p.getReference(), p));

                    // 1 seule page dans la branche : l'entrée porte le nom de la branche.
                    // Plusieurs pages : chaque entrée porte le libellé (court) de sa permission.
                    boolean pageUnique = parUrl.size() == 1;
                    List<EntreeMenuDto> entrees = parUrl.values().stream()
                            .map(p -> new EntreeMenuDto(
                                    pageUnique ? branche.getLibelle() : libelleCourt(p.getLibelle()),
                                    branche.getIcone(), p.getReference(), p.getCode()))
                            .toList();
                    return new SectionMenuDto(branche.getCode(), branche.getLibelle(),
                            branche.getIcone(), entrees);
                })
                .filter(SectionMenuDto::isNonVide)
                .toList();
    }

    /** « Consulter les personnes » → « Personnes ». */
    private static String libelleCourt(String libelle) {
        if (libelle == null) {
            return "";
        }
        String l = libelle.replaceFirst("(?i)^consulter (les |le |la |l')?", "").trim();
        return l.isEmpty() ? libelle : Character.toUpperCase(l.charAt(0)) + l.substring(1);
    }
}
