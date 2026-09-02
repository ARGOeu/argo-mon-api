package org.grnet.status.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.endpoint.scanner.runtime.context.RoleEndpointHolder;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;
import org.grnet.status.dtos.incident.IncidentRequestDto;
import org.grnet.status.dtos.incident.IncidentResponseDto;
import org.grnet.status.entities.Contact;
import org.grnet.status.entities.Incident;
import org.grnet.status.entities.IncidentActivity;
import org.grnet.status.mappers.IncidentActivityMapper;
import org.grnet.status.mappers.IncidentMapper;
import org.grnet.status.repositories.IncidentActivityRepository;
import org.grnet.status.repositories.IncidentRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.dtos.incident.*;
import org.grnet.status.entities.IncidentComment;
import org.grnet.status.repositories.IncidentCommentRepository;
import org.grnet.status.entities.IncidentAffectedService;


import java.time.Instant;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.dtos.pagination.PageResource;

import java.time.Year;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Inject
    AccessControlService accessControlService;

    @Inject
    EntitlementProvider entitlementProvider;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String parentGroup;


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

        incident.getServices()
                .forEach(service -> service.setIncident(incident));

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

                var serviceNames = incident.getServices()
                        .stream()
                        .map(IncidentAffectedService::getServiceName)
                        .toList();

                mailerService.sendIncidentCreatedEmail(
                        recipientEmails,
                        incident.getIncidentNumber(),
                        incident.getTitle(),
                        incident.getDescription(),
                        serviceNames,
                        incident.getStatus().name(),
                        incident.getCreatedBy(),
                        incident.getCreatedAt().toString(),
                        incidentUrl
                );

                Log.infof(
                        "Sending incident %s email to %d recipient(s) for services: %s",
                        incident.getIncidentNumber(),
                        recipientEmails.size(),
                        serviceNames
                );
            }

        } catch (Exception e) {
            Log.warn("Incident reported email notification failed.", e);
        }

        return IncidentMapper.INSTANCE.incidentToResponseDto(incident);
    }

    @Transactional
    public IncidentResponseDto updateIncidentStatus(List<RoleEndpoint> roles, String tenantId, String incidentId, IncidentUpdateRequestDto request, String updatedBy) {

        var incident = getIncidentForModification(roles, tenantId, incidentId, updatedBy);

        var previousStatus = incident.getStatus();

        if (request.status != null) {

            if (request.status != previousStatus) {

                incident.setStatus(request.status);
                incident.setStatusDescription(request.statusDescription);

                var activity = new IncidentActivity();
                activity.setIncident(incident);
                activity.setPreviousStatus(previousStatus);
                activity.setNewStatus(request.status);
                activity.setStatusDescription(request.statusDescription);
                activity.setChangedBy(updatedBy);

                incidentActivityRepository.persist(activity);

            } else if (!Objects.equals(request.statusDescription, incident.getStatusDescription())) {

                incident.setStatusDescription(request.statusDescription);

                var latestActivity = incidentActivityRepository.findLatestByIncidentId(incidentId);

                if (latestActivity != null && latestActivity.getNewStatus() == request.status) {
                    latestActivity.setStatusDescription(request.statusDescription);
                    latestActivity.setChangedBy(updatedBy);
                }
            }
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


    public PageResource<IncidentResponseDto> getIncidentsByPageAndSize(String tenantId, int page, int size, String search, String date, UriInfo uriInfo) {

        var incidents = incidentRepository.fetchIncidentsByTenantIdByPageAndSize(tenantId, page, size, search, date);

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


    private Incident getIncidentForModification(List<RoleEndpoint> roles, String tenantId, String incidentId, String userId) {

        var incident = incidentRepository.fetchByIdAndTenantId(incidentId, tenantId);

        if (accessControlService.isSuperAdmin()) {
            return incident;
        }

        var userRoles = getRolesFromEntitlements(
                entitlementProvider.fetchEntitlements()
                        .stream()
                        .map(Entitlement::getRaw)
                        .collect(Collectors.toList())
        );

        var scope = roles.stream()
                .filter(role -> userRoles.contains(role.getRoleName()))
                .map(RoleEndpoint::getScope)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(s -> s.equalsIgnoreCase("ALL") ? 1 : 0))
                .orElse(null);

        if (Objects.isNull(scope)) {
            throw new ForbiddenException("Scope must be defined for this endpoint!");
        }

        var resolvedScope = Scope.valueOf(scope.toUpperCase());

        if (resolvedScope == Scope.MINE
                && !Objects.equals(incident.getCreatedBy(), userId)) {

            throw new ForbiddenException("You are not allowed to modify this incident.");
        }

        return incident;
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

    private Scope resolveScope() {

        if (accessControlService.isSuperAdmin()) {
            return Scope.ALL;
        }

        var roles = RoleEndpointHolder.get();

        var userRoles = getRolesFromEntitlements(
                entitlementProvider.fetchEntitlements()
                        .stream()
                        .map(Entitlement::getRaw)
                        .collect(Collectors.toList())
        );

        var scope = roles.stream()
                .filter(role -> userRoles.contains(role.getRoleName()))
                .map(RoleEndpoint::getScope)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() ->
                        new ForbiddenException(
                                "Scope must be defined for this endpoint!"
                        )
                );

        return Scope.valueOf(scope.toUpperCase());
    }

    private List<String> getRolesFromEntitlements(List<String> rawEntitlements) {

        return rawEntitlements.stream()
                .map(ent -> {
                    var idx = ent.indexOf(parentGroup);

                    if (idx == -1) { return null; }

                    var remaining = ent.substring(idx + parentGroup.length() + 1);

                    return remaining.split(":")[0];
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
