package org.grnet.status.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.dtos.incident.IncidentRequestDto;
import org.grnet.status.dtos.incident.IncidentResponseDto;
import org.grnet.status.entities.Contact;
import org.grnet.status.entities.Incident;
import org.grnet.status.entities.IncidentActivity;
import org.grnet.status.enums.IncidentStatus;
import org.grnet.status.mappers.IncidentActivityMapper;
import org.grnet.status.mappers.IncidentMapper;
import org.grnet.status.repositories.IncidentActivityRepository;
import org.grnet.status.repositories.IncidentRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.dtos.incident.*;
import org.grnet.status.entities.IncidentComment;
import org.grnet.status.repositories.IncidentCommentRepository;


import java.time.Instant;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.dtos.pagination.PageResource;

import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class IncidentService {

    @Inject
    IncidentRepository incidentRepository;

    @Inject
    TenantRepository tenantRepository;

    @Inject
    IncidentCommentRepository incidentCommentRepository;

    @Inject
    IncidentActivityRepository incidentActivityRepository;

    @Inject
    MailerService mailerService;

    @ConfigProperty(name = "api.ui.url")
    String uiBaseUrl;

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
        incident.setCreatedBy(createdBy);

        incidentRepository.persist(incident);

        var recipientEmails = incident.getTenant()
                .getContacts()
                .stream()
                .map(Contact::getContactEmail)
                .filter(Objects::nonNull)
                .filter(email -> !email.isBlank())
                .distinct()
                .toList();

        try {
            if (!recipientEmails.isEmpty()) {

                var incidentUrl = uiBaseUrl + "/tenants/" + tenantId + "/incidents/" + incident.getId();

                mailerService.sendIncidentCreatedEmail(
                        recipientEmails,
                        incident.getIncidentNumber(),
                        incident.getTitle(),
                        incident.getDescription(),
                        incident.getServiceName(),
                        incident.getStatus().name(),
                        incident.getCreatedBy(),
                        incident.getCreatedAt().toString(),
                        incidentUrl
                );
            }

        } catch (Exception e) {
            Log.warn("Incident reported email notification failed.", e);
        }

        return IncidentMapper.INSTANCE.incidentToResponseDto(incident);
    }

    @Transactional
    public IncidentResponseDto updateIncident(String tenantId, String incidentId, IncidentUpdateRequestDto request, String updatedBy) {

        var incident = incidentRepository.fetchByIdAndTenantId(incidentId, tenantId);

        var previousStatus = incident.getStatus();

        if (request.status != null && request.status != previousStatus) {

            incident.setStatus(request.status);

            var activity = new IncidentActivity();
            activity.setIncident(incident);
            activity.setPreviousStatus(previousStatus);
            activity.setNewStatus(request.status);
            activity.setChangedBy(updatedBy);

            incidentActivityRepository.persist(activity);
        }

        incident.setUpdatedAt(Instant.now());
        incident.setUpdatedBy(updatedBy);

        return IncidentMapper.INSTANCE.incidentToResponseDto(incident);
    }

    @Transactional
    public IncidentResponseDto addComment(String tenantId, String incidentId, IncidentCommentRequestDto request, String userId) {

        var incident = incidentRepository.fetchByIdAndTenantId(incidentId, tenantId);

        var comment = new IncidentComment();

        comment.setIncident(incident);
        comment.setComment(request.comment.trim());
        comment.setCreatedBy(userId);

        incidentCommentRepository.persist(comment);

        incident.getComments().add(comment);
        incident.setUpdatedAt(Instant.now());

        return IncidentMapper.INSTANCE.incidentToResponseDto(incident);
    }


    public PageResource<IncidentResponseDto> getIncidentsByPageAndSize(String tenantId, int page, int size, String search, UriInfo uriInfo) {

        var incidents = incidentRepository.fetchIncidentsByTenantIdByPageAndSize(tenantId, page, size, search);

        return new PageResource<>(incidents, IncidentMapper.INSTANCE.incidentsToDtos(incidents.list()), uriInfo);
    }

    public IncidentResponseDto getIncident(String tenantId, String incidentId) {

        var incident = incidentRepository.fetchByIdAndTenantId(incidentId, tenantId);

        return IncidentMapper.INSTANCE.incidentToResponseDto(incident);
    }


    public List<IncidentActivityResponseDto> getIncidentActivity(String tenantId, String incidentId) {

        var activities = incidentActivityRepository.fetchByIncidentId(incidentId);

        return IncidentActivityMapper.INSTANCE.incidentActivitiesToDtos(activities);
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
