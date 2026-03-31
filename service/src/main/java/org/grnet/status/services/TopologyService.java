package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.topology.EndpointTopologyDto;
import org.grnet.status.dtos.topology.GroupTopologyDto;
import org.grnet.status.dtos.topology.ServiceTypeDto;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.clients.WebApiService;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class TopologyService {
    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @Inject
    WebApiService webApiService;

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;

    private static final Logger LOG = Logger.getLogger(TopologyService.class);

    public List<GroupTopologyDto> fetchGroupTopologies(String id, String date) {

        LOG.info("Fetching group topology from ARGO Web API...");
        webApiService.validateTenantInitialized(id, "Group Topology");
        var response = argoWebApiClient.fetchTopologyGroupsSuperAdmin(accessToken, id, date);
        return response.data;
    }

    public List<EndpointTopologyDto> fetchEndpointTopologies(String id, String date) {

        LOG.info("Fetching endpoint topology from ARGO Web API...");
        webApiService.validateTenantInitialized(id, "Endpoint Topology");
        var response = argoWebApiClient.fetchTopologyEndpointsSuperAdmin(accessToken, id, date);
        return response.data;
    }

    public List<ServiceTypeDto> fetchServiceTypes(String id, String date) {

        LOG.info("Fetching service types from ARGO Web API...");
        webApiService.validateTenantInitialized(id, "Service Type");
        var response = argoWebApiClient.fetchServiceTypesSuperAdmin(accessToken, id, date);
        return response.data;
    }

    public Status createGroupTopology(String id, String date, Boolean force, List<GroupTopologyDto> request) {

        LOG.info("Creating group topology to ARGO Web API...");
        try {


            webApiService.validateTenantInitialized(id, "Group Topology");

            force = Boolean.TRUE.equals(force) ? Boolean.TRUE : null;
            return argoWebApiClient.createTopologyGroupsSuperAdmin(accessToken, id, date, force, request);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

    public Status createEndpointTopology(String id, String date, Boolean force, List<EndpointTopologyDto> request) {

        LOG.info("Creating endpoint topology to ARGO Web API...");

        try {
            // Validate tenant and create topology
            webApiService.validateTenantInitialized(id, "Endpoint Topology");

            force = Boolean.TRUE.equals(force) ? Boolean.TRUE : null;
            return argoWebApiClient.createTopologyEndpointsSuperAdmin(accessToken, id, date, force, request);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }


    public Status deleteGroupTopology(String id, String date) {

        LOG.info("Deleting group topology from ARGO Web API...");
        try {
            webApiService.validateTenantInitialized(id, "Group Topology");
            return argoWebApiClient.deleteTopologyGroupsSuperAdmin(accessToken, id, date);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

    public Status deleteEndpointTopology(String id, String date) {

        LOG.info("Deleting endpoint topology from ARGO Web API...");
        try {
            webApiService.validateTenantInitialized(id, "Endpoint Topology");
            return argoWebApiClient.deleteTopologyEndpointsSuperAdmin(accessToken, id, date);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

    public Status deleteServiceTypes(String id, String date) {

        LOG.info("Deleting service types from ARGO Web API...");
        try {
            webApiService.validateTenantInitialized(id, "Service Type");
            return argoWebApiClient.deleteServiceTypesSuperAdmin(accessToken, id, date);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

    public Status createServiceTypes(String id, String date, Boolean force, List<ServiceTypeDto> request) {

        LOG.info("Creating service types to ARGO Web API...");

        try {
            webApiService.validateTenantInitialized(id, "Service Type");

            force = Boolean.TRUE.equals(force) ? Boolean.TRUE : null;
            return argoWebApiClient.createServiceTypesSuperAdmin(accessToken, id, date, force, request);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }
    public void findTheRules() {

        String parentGroup = "status-pages";
        String path = "/tenants/{id}/topology/{topology-id}/endpoints";

        List<String> pathParts = parsePath(path);

        Set<String> rules = new HashSet<>();
        String[] entitlements = {
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:LOCALTENANT:role=admin",
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANT-TEST:role=admin",
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANTB:role=admin",
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANT-TEST:role=viewer",
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANTB:role=viewer",
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANTB:topology:5:role=viewer",
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANTB:topology:6:role=viewer",
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:members:role=member"
        };
        for (String ent : entitlements) {

            String[] parts = normalize(ent, parentGroup);
            if (parts == null) continue;

            String template = buildTemplate(parts, pathParts);

            rules.add(".*:" + template);
        }

        rules.forEach(System.out::println);
    }
    private String buildTemplate(String[] entParts, List<String> pathParts) {

        List<String> result = new ArrayList<>();

        int pathIndex = 0;

        for (int i = 0; i < entParts.length; i++) {

            String entPart = entParts[i];

            // role=... always keep as-is
            if (entPart.startsWith("role=")) {
                result.add(entPart);
                continue;
            }

            if (pathIndex >= pathParts.size()) {
                result.add(entPart);
                continue;
            }

            String pathPart = pathParts.get(pathIndex);

            if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
                // replace with placeholder
                result.add(pathPart);
            } else {
                // must match static segment
                if (pathPart.equals(entPart)) {
                    result.add(entPart);
                } else {
                    // mismatch → stop alignment
                    result.add(entPart);
                }
            }

            pathIndex++;
        }

        return String.join(":", result);
    }
    private String[] normalize(String ent, String parentGroup) {
        int idx = ent.indexOf("group:" + parentGroup);
        if (idx == -1) return null;

        String relevant = ent.substring(idx + ("group:" + parentGroup).length() + 1);
        return relevant.split(":");
    }
    private List<String> parsePath(String path) {
        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
