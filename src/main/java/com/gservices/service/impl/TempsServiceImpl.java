package com.gservices.service.impl;

import com.gservices.dto.AnneeDto;
import com.gservices.dto.JourDto;
import com.gservices.dto.MoisDto;
import com.gservices.entity.Annee;
import com.gservices.entity.Jour;
import com.gservices.entity.Mois;
import com.gservices.exception.BusinessException;
import com.gservices.exception.ResourceNotFoundException;
import com.gservices.mapper.AnneeMapper;
import com.gservices.mapper.JourMapper;
import com.gservices.mapper.MoisMapper;
import com.gservices.repository.AnneeRepository;
import com.gservices.repository.JourRepository;
import com.gservices.repository.MoisRepository;
import com.gservices.service.TempsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class TempsServiceImpl implements TempsService {

    private static final Locale FR = Locale.FRENCH;

    private final JourRepository jourRepo;
    private final AnneeRepository anneeRepo;
    private final MoisRepository moisRepo;
    private final JourMapper jourMapper;
    private final AnneeMapper anneeMapper;
    private final MoisMapper moisMapper;

    public TempsServiceImpl(JourRepository jourRepo, AnneeRepository anneeRepo, MoisRepository moisRepo,
                            JourMapper jourMapper, AnneeMapper anneeMapper, MoisMapper moisMapper) {
        this.jourRepo = jourRepo;
        this.anneeRepo = anneeRepo;
        this.moisRepo = moisRepo;
        this.jourMapper = jourMapper;
        this.anneeMapper = anneeMapper;
        this.moisMapper = moisMapper;
    }

    // ---------------------------------------------------------------- Jours

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_LIRE)")
    public List<JourDto> jours() {
        return jourRepo.findAllByOrderByNumeroJourSemaineAsc().stream().map(jourMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_GERER)")
    public JourDto modifierJour(Long id, JourDto dto) {
        Jour j = jourRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Jour.class, id));
        jourMapper.update(j, dto);
        return jourMapper.toDto(j);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_GERER)")
    public void basculerJour(Long id) {
        Jour j = jourRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Jour.class, id));
        j.setEtat(!j.isEtat());
    }

    // ---------------------------------------------------------------- Années

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_LIRE)")
    public List<AnneeDto> annees() {
        return anneeRepo.findAllByOrderByValeurAnneeDesc().stream().map(anneeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_LIRE)")
    public AnneeDto annee(Long id) {
        return anneeMapper.toDto(anneeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Annee.class, id)));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_GERER)")
    public AnneeDto creerAnnee(int valeurAnnee) {
        if (valeurAnnee < 1900 || valeurAnnee > 2200) {
            throw new BusinessException("L'année doit être comprise entre 1900 et 2200.");
        }
        if (anneeRepo.existsByValeurAnnee(valeurAnnee)) {
            throw new BusinessException("L'année " + valeurAnnee + " existe déjà.");
        }
        boolean bissextile = LocalDate.ofYearDay(valeurAnnee, 1).lengthOfYear() == 366;

        Annee annee = new Annee();
        annee.setLibelle(String.valueOf(valeurAnnee));
        annee.setValeurAnnee(valeurAnnee);
        annee.setEstBissextile(bissextile);
        annee.setDateDebut(LocalDate.of(valeurAnnee, 1, 1));
        annee.setDateFin(LocalDate.of(valeurAnnee, 12, 31));

        for (Month m : Month.values()) {
            Mois mois = new Mois();
            String nom = m.getDisplayName(TextStyle.FULL, FR);
            mois.setLibelle(Character.toUpperCase(nom.charAt(0)) + nom.substring(1));
            mois.setAbreviation(capitaliser(m.getDisplayName(TextStyle.SHORT, FR)));
            mois.setNumeroMois(m.getValue());
            mois.setNombreJours(m.length(bissextile));
            annee.addMois(mois);
        }
        return anneeMapper.toDto(anneeRepo.save(annee));
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_GERER)")
    public void basculerAnnee(Long id) {
        Annee a = anneeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Annee.class, id));
        a.setEtat(!a.isEtat());
    }

    // ---------------------------------------------------------------- Mois

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_LIRE)")
    public List<MoisDto> moisParAnnee(Long idAnnee) {
        return moisRepo.findByAnneeIdOrderByNumeroMoisAsc(idAnnee).stream().map(moisMapper::toDto).toList();
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_GERER)")
    public MoisDto modifierMois(Long id, MoisDto dto) {
        Mois m = moisRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Mois.class, id));
        if (dto.getLibelle() != null && !dto.getLibelle().isBlank()) {
            m.setLibelle(dto.getLibelle().trim());
        }
        m.setAbreviation(dto.getAbreviation());
        return moisMapper.toDto(m);
    }

    @Override
    @PreAuthorize("hasAuthority(T(com.gservices.security.Perms).TEMPS_GERER)")
    public void basculerMois(Long id) {
        Mois m = moisRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(Mois.class, id));
        m.setEtat(!m.isEtat());
    }

    private static String capitaliser(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        String base = s.replace(".", "");
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }
}
