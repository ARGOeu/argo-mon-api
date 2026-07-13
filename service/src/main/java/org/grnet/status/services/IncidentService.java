package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.grnet.status.dtos.incident.IncidentRequestDto;
import org.grnet.status.dtos.incident.IncidentResponseDto;
import org.grnet.status.entities.Incident;
import org.grnet.status.enums.IncidentStatus;
import org.grnet.status.mappers.IncidentMapper;
import org.grnet.status.repositories.IncidentRepository;
import org.grnet.status.repositories.TenantRepository;

import java.time.Year;
import java.time.ZoneOffset;

@ApplicationScoped
public class IncidentService {

    @Inject
    IncidentRepository incidentRepository;

    @Inject
    TenantRepository tenantRepository;

    /**
     * Creates a new incident for a tenant.
     *
     * @param tenantId  tenant identifier
     * @param request   incident request
     * @param createdBy name of the authenticated user reporting the incident
     * @return created incident
     */
    @Transactional
    public IncidentResponseDto createIncident(String tenantId, IncidentRequestDto request, String createdBy) {

        var tenant = tenantRepository.findById(tenantId);

        var incident = IncidentMapper.INSTANCE.incidentRequestToEntity(request);

        incident.setTenant(tenant);
        incident.setIncidentNumber(generateIncidentNumber());
        incident.setStatus(IncidentStatus.REPORTED);
        incident.setCreatedBy(createdBy);

        incidentRepository.persist(incident);

        return IncidentMapper.INSTANCE.incidentToResponseDto(incident);
    }


    /**
     * Generates the human-readable incident number.
     *
     * Example: INC-2026-000001
     */
    private String generateIncidentNumber() {
        var sequenceValue =
                incidentRepository.nextIncidentSequenceValue();

        var year = Year.now(ZoneOffset.UTC).getValue();

        return String.format(
                "INC-%d-%06d",
                year,
                sequenceValue
        );
    }
}
