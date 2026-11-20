package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.grnet.status.dtos.argo.web.api.ArgoCreateRecomputationResponse;
import org.grnet.status.dtos.argo.web.api.ArgoRecomputationResponse;
import org.grnet.status.dtos.profile.metric.ServiceMetrics;
import org.grnet.status.dtos.recomputations.AppliedStatusChangeDto;
import org.grnet.status.dtos.recomputations.ExcludeMetricDto;
import org.grnet.status.dtos.recomputations.RecomputationElement;
import org.grnet.status.dtos.recomputations.RecomputationRequestDto;
import org.grnet.status.dtos.recomputations.RecomputationUpdateResponse;
import org.grnet.status.enums.ArgoItemStatusEnum;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.clients.WebApiService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Service responsible for managing tenant's recomputation workflows.
 */
@ApplicationScoped
public class RecomputationService {

    @Inject
    TenantRepository tenantRepository;
    @Inject
    WebApiService webApiService;

    @Inject
    ReportService reportService;

    @Transactional
    public ArgoCreateRecomputationResponse createRecomputation(String tenantId, RecomputationRequestDto request) {

        var tenant = tenantRepository.findById(tenantId);
        var reports = webApiService.retrieveReportsWebApi(tenantId, false, false, false);

        var report = reports.data.stream()
                .filter(r -> r.info.name.equals(request.getReport()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Report '" + request.getReport() + "' not found in tenant's reports"));

        var elements = getRecomputationElements(tenantId, report.id);
        validateExcludeGroups(request.getExclude(), elements);
        validateExcludeMetrics(request.getExcludeMetrics(), elements);
        validateAppliedStatusChanges(request.getAppliedStatusChanges(), elements);

        return webApiService.createRecomputation(tenant.id, request);
    }

    @Transactional
    public RecomputationUpdateResponse updateRecomputation(String tenantId, String recomputationId, RecomputationRequestDto request) {

        var tenant = tenantRepository.findById(tenantId);
        //var recomputation= webApiService.getRecomputationById(tenant.id, recomputationId);
        var reports = webApiService.retrieveReportsWebApi(tenantId, false, false, false);


        var report = reports.data.stream()
                .filter(r -> r.info.name.equals(request.getReport()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Report '" + request.getReport() + "' not found in tenant's reports"));

        var elements = getRecomputationElements(tenantId, report.id);
        validateExcludeGroups(request.getExclude(), elements);
        validateExcludeMetrics(request.getExcludeMetrics(), elements);
        validateAppliedStatusChanges(request.getAppliedStatusChanges(), elements);

        return webApiService.updateRecomputation(tenant.id, recomputationId,request);
    }

    @Transactional
    public ArgoRecomputationResponse getRecomputations(String tenantId, String date, String reportId) {

        var tenant = tenantRepository.findById(tenantId);

        String reportName = null;

        if (reportId != null && !reportId.isBlank()) {
            var report = reportService.fetchReportById(tenantId, reportId);
            reportName = report.info.name;
        }

        return webApiService.getRecomputations(tenant.id, date, reportName);
    }

    @Transactional
    public ArgoRecomputationResponse getRecomputationById(String tenantId, String recomputationId) {

        var tenant = tenantRepository.findById(tenantId);

        return webApiService.getRecomputationById(tenant.id, recomputationId);
    }

    @Transactional
    public void deleteRecomputationById(String tenantId, String recomputationId) {

        var tenant = tenantRepository.findById(tenantId);

        webApiService.deleteRecomputationById(tenant.id, recomputationId);
    }

    @Transactional
    public List<RecomputationElement> getRecomputationElements(String tenantId, String reportId) {

        var tenant = tenantRepository.findById(tenantId);
        var report = reportService.fetchReportById(tenantId, reportId);

        var metricId = report.profiles.stream()
                .filter(p -> "metric".equals(p.type))
                .map(p -> p.id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "No metric profile found for report: " + reportId
                ));

        var metricProfile = webApiService.listSpecificMetricProfiles(tenantId, metricId);
        //  var topologyGroups = webApiService.listTopologyGroupsByReport(tenantId,report.info.name);
        var topologyEndpoints = webApiService.listTopologyEndpointsByReport(tenantId, report.info.name);

        var services = metricProfile.getData().get(0).getServices();

        Map<String, List<String>> metricsByService = services.stream()
                .collect(Collectors.toMap(
                        ServiceMetrics::getService,
                        ServiceMetrics::getMetrics,
                        (existing, replacement) -> existing
                ));

        List<RecomputationElement> recomputationElements = topologyEndpoints.data.stream()
                .flatMap(endpoint -> metricsByService
                        .getOrDefault(endpoint.getService(), List.of())
                        .stream()
                        .map(metric -> new RecomputationElement(
                                metric,
                                endpoint.getHostname(),
                                endpoint.getService(),
                                endpoint.getGroup()
                        )))
                .distinct()
                .toList();

        return recomputationElements;
    }

    private boolean matches(
            RecomputationElement element,
            String metric,
            String hostname,
            String service,
            String group
    ) {
        return matchesField(element.metric(), metric)
                && matchesField(element.hostname(), hostname)
                && matchesField(element.service(), service)
                && matchesField(element.group(), group);
    }

    private boolean matchesField(String actual, String expected) {
        return expected == null || expected.isBlank() || expected.equals(actual);
    }
    private void validateAppliedStatusChanges(List<AppliedStatusChangeDto> statusChanges, List<RecomputationElement> elements) {
        if (statusChanges == null) {
            return;
        }

        for (var change : statusChanges) {
            if (!ArgoItemStatusEnum.isValid(change.getState())) {
                throw new BadRequestException(
                        "Invalid status '" + change.getState() +
                                "'. Allowed values: " + Arrays.toString(ArgoItemStatusEnum.values())
                );
            }

            boolean exists = elements.stream()
                    .anyMatch(element -> matches(
                            element,
                            change.getMetric(),
                            change.getHostname(),
                            change.getService(),
                            change.getGroup()
                    ));

            if (!exists) {
                throw new BadRequestException(
                        "Element in Applied status change does not exist in report topology"
                );
            }
        }
    }

    private void validateExcludeMetrics(List<ExcludeMetricDto> excludeMetrics, List<RecomputationElement> elements) {
        if (excludeMetrics == null) {
            return;
        }

        for (var excluded : excludeMetrics) {
            boolean exists = elements.stream().anyMatch(element -> matches(element, excluded.getMetric(), excluded.getHostname(), excluded.getService(), excluded.getGroup()));

            if (!exists) {
                throw new BadRequestException(
                        "Element in Exclude metric does not exist in report topology: " + excluded.toString()
                );
            }
        }
    }
    private void validateExcludeGroups(List<String> exclude, List<RecomputationElement> elements) {
        if (exclude == null) {
            return;
        }

        var availableGroups = elements.stream()
                .map(RecomputationElement::group)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (var group : exclude) {
            if (!availableGroups.contains(group)) {
                throw new BadRequestException("Invalid group in exclude: '" + group + "'");
            }
        }
    }
}
