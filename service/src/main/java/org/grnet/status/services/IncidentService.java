package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.grnet.status.dtos.incident.*;
import org.grnet.status.entities.IncidentComment;
import org.grnet.status.enums.IncidentStatus;
import org.grnet.status.exceptions.BadRequestException;
import org.grnet.status.mappers.IncidentCommentMapper;
import org.grnet.status.mappers.IncidentMapper;
import org.grnet.status.repositories.IncidentCommentRepository;
import org.grnet.status.repositories.IncidentRepository;
import org.grnet.status.repositories.TenantRepository;

import java.time.Instant;
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

    @Inject
    IncidentCommentRepository incidentCommentRepository;

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

    @Transactional
    public IncidentResponseDto updateIncident(String tenantId, String incidentId, IncidentUpdateRequestDto request, String updatedBy) {

        var incident = incidentRepository
                .fetchByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new NotFoundException("There is no Incident with the following id: " + incidentId));

        if (request.status != null) {
            incident.setStatus(request.status);
        }

        incident.setUpdatedAt(Instant.now());
        incident.setUpdatedBy(updatedBy);

        return IncidentMapper.INSTANCE.incidentToResponseDto(incident);
    }

    @Transactional
    public IncidentResponseDto addComment(String tenantId, String incidentId, IncidentCommentRequestDto request, String userId) {
        var incident = incidentRepository
                .fetchByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new NotFoundException(
                        "There is no Incident with the following id: " + incidentId
                ));

        var comment = new IncidentComment();

        comment.setIncident(incident);
        comment.setComment(request.comment.trim());
        comment.setCreatedBy(userId);

        incidentCommentRepository.persist(comment);

        incident.getComments().add(comment);
        incident.setUpdatedAt(Instant.now());

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
