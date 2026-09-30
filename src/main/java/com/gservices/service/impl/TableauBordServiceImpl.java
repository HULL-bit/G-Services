package com.gservices.service.impl;

import com.gservices.dto.PersonneDto;
import com.gservices.dto.RepartitionDto;
import com.gservices.dto.TableauBordDto;
import com.gservices.entity.Permission;
import com.gservices.mapper.PersonneMapper;
import com.gservices.repository.BranchePermissionRepository;
import com.gservices.repository.PermissionRepository;
import com.gservices.repository.PersonneRepository;
import com.gservices.repository.ProfilRepository;
import com.gservices.service.TableauBordService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TableauBordServiceImpl implements TableauBordService {

    private final PersonneRepository personneRepository;
    private final ProfilRepository profilRepository;
    private final PermissionRepository permissionRepository;
    private final BranchePermissionRepository brancheRepository;
    private final PersonneMapper personneMapper;

    public TableauBordServiceImpl(PersonneRepository personneRepository,
                                  ProfilRepository profilRepository,
                                  PermissionRepository permissionRepository,
                                  BranchePermissionRepository brancheRepository,
                                  PersonneMapper personneMapper) {
        this.personneRepository = personneRepository;
        this.profilRepository = profilRepository;
        this.permissionRepository = permissionRepository;
        this.brancheRepository = brancheRepository;
        this.personneMapper = personneMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).DASHBOARD_LIRE)")
    public TableauBordDto chiffres() {
        return new TableauBordDto(
                personneRepository.count(),
                personneRepository.countByEtatTrue(),
                personneRepository.countByVerrouilleJusquaAfter(LocalDateTime.now()),
                profilRepository.count(),
                permissionRepository.count(),
                brancheRepository.count());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).DASHBOARD_LIRE)")
    public List<RepartitionDto> repartitionPermissions() {
        List<Object[]> lignes = new ArrayList<>();
        brancheRepository.findByEtatTrueOrderByNiveauAsc().forEach(b -> {
            long nb = b.getPermissions().stream().filter(Permission::isEtat).count();
            if (nb > 0) {
                lignes.add(new Object[]{b.getLibelle(), nb});
            }
        });
        long max = lignes.stream().mapToLong(l -> (long) l[1]).max().orElse(1);
        List<RepartitionDto> res = new ArrayList<>();
        for (Object[] l : lignes) {
            long v = (long) l[1];
            res.add(new RepartitionDto((String) l[0], v, (int) Math.round(v * 100.0 / max)));
        }
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).DASHBOARD_LIRE)")
    public List<PersonneDto> comptesVerrouilles() {
        return personneRepository
                .findByVerrouilleJusquaAfterOrderByVerrouilleJusquaDesc(LocalDateTime.now())
                .stream().map(personneMapper::toDto).toList();
    }
}
