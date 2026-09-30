package com.gservices.web.bean;

import com.gservices.dto.PersonneDto;
import com.gservices.dto.RepartitionDto;
import com.gservices.dto.TableauBordDto;
import com.gservices.service.TableauBordService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.Serializable;
import java.util.List;

@Component("dashboardBean")
@RequestScope
public class DashboardBean implements Serializable {

    private final transient TableauBordService service;

    private TableauBordDto chiffres;
    private List<RepartitionDto> repartition;
    private List<PersonneDto> comptesVerrouilles;

    public DashboardBean(TableauBordService service) {
        this.service = service;
    }

    @PostConstruct
    void init() {
        this.chiffres = service.chiffres();
        this.repartition = service.repartitionPermissions();
        this.comptesVerrouilles = service.comptesVerrouilles();
    }

    public TableauBordDto getChiffres() { return chiffres; }
    public List<RepartitionDto> getRepartition() { return repartition; }
    public List<PersonneDto> getComptesVerrouilles() { return comptesVerrouilles; }
}
