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
//    public void findTheRules() {
//
//        String parentGroup = "status-pages";
//        String path = "/tenants/{id}/topology/{topology-id}/endpoints";
//
//        List<String> pathParts = parsePath(path);
//
//        Set<String> rules = new HashSet<>();
//        String[] entitlements = {
//                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:LOCALTENANT:role=admin",
//                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANT-TEST:role=admin",
//                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANTB:role=admin",
//                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANT-TEST:role=viewer",
//                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANTB:role=viewer",
//                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANTB:topology:5:role=viewer",
//                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:tenants:TENANTB:topology:6:role=viewer",
//                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:members:role=member"
//        };
//        for (String ent : entitlements) {
//
//            String[] parts = normalize(ent, parentGroup);
//            if (parts == null) continue;
//
//            String template = buildTemplate(parts, pathParts);
//
//            rules.add(".*:" + template);
//        }
//
//        rules.forEach(System.out::println);
//    }
//    private String buildTemplate(String[] entParts, List<String> pathParts) {
//
//        List<String> result = new ArrayList<>();
//
//        int pathIndex = 0;
//
//        for (int i = 0; i < entParts.length; i++) {
//
//            String entPart = entParts[i];
//
//            // role=... always keep as-is
//            if (entPart.startsWith("role=")) {
//                result.add(entPart);
//                continue;
//            }
//
//            if (pathIndex >= pathParts.size()) {
//                result.add(entPart);
//                continue;
//            }
//
//            String pathPart = pathParts.get(pathIndex);
//
//            if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
//                // replace with placeholder
//                result.add(pathPart);
//            } else {
//                // must match static segment
//                if (pathPart.equals(entPart)) {
//                    result.add(entPart);
//                } else {
//                    // mismatch → stop alignment
//                    result.add(entPart);
//                }
//            }
//
//            pathIndex++;
//        }
//
//        return String.join(":", result);
//    }
//    private String[] normalize(String ent, String parentGroup) {
//        int idx = ent.indexOf("group:" + parentGroup);
//        if (idx == -1) return null;
//
//        String relevant = ent.substring(idx + ("group:" + parentGroup).length() + 1);
//        return relevant.split(":");
//    }
//    private List<String> parsePath(String path) {
//        return Arrays.stream(path.split("/"))
//                .filter(s -> !s.isEmpty())
//                .collect(Collectors.toList());
//    }
//


    public void findTheRules() {

                List<String> entitlements = List.of(
                        "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:LOCALTENANT:role=admin",
                        "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:TENANT-TEST:role=admin",
                        "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:TENANTB:role=admin",
                        "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:TENANT-TEST:role=viewer",
                        "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:TENANTB:role=viewer",
                        "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:TENANTB:topology:5:role=viewer",
                        "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:TENANTB:topology:6:role=viewer",
                        "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:members:role=member"
                );


                // Step 1: Infer raw patterns
                List<String> rawPatterns = inferPatterns(entitlements);

                // Step 2: Merge patterns (IMPORTANT)
                List<String> finalPatterns = mergePatterns(rawPatterns);

                System.out.println("Final Patterns:");
                finalPatterns.forEach(System.out::println);
            }

            // =========================
            // PIPELINE
            // =========================

            public List<String> inferPatterns(List<String> entitlements) {

                Map<String, List<List<String>>> clusters = cluster(entitlements);

                List<String> results = new ArrayList<>();

                for (List<List<String>> group : clusters.values()) {

                    TrieNode root = new TrieNode();

                    for (List<String> tokens : group) {
                        insert(root, tokens);
                    }

                    buildPatterns(root, new StringBuilder("*"), 0, results);
                }

                return results;
            }

            // =========================
            // CLUSTERING
            // =========================

            private Map<String, List<List<String>>> cluster(List<String> entitlements) {

                Map<String, List<List<String>>> clusters = new HashMap<>();

                for (String e : entitlements) {

                    List<String> tokens = Arrays.asList(e.split(":"));

                    String key = normalizeStructure(tokens);

                    clusters.computeIfAbsent(key, k -> new ArrayList<>())
                            .add(tokens);
                }

                return clusters;
            }

            private String normalizeStructure(List<String> tokens) {

                List<String> normalized = new ArrayList<>();

                for (String t : tokens) {
                    if (t.contains("=")) {
                        normalized.add("ATTR");
                    } else if (t.matches("\\d+")) {
                        normalized.add("NUM");
                    } else {
                        normalized.add("STR");
                    }
                }

                return String.join("-", normalized);
            }

            // =========================
            // TRIE
            // =========================

            static class TrieNode {
                Map<String, TrieNode> children = new HashMap<>();
            }

            private void insert(TrieNode root, List<String> tokens) {
                TrieNode node = root;

                for (String token : tokens) {
                    node = node.children.computeIfAbsent(token, k -> new TrieNode());
                }
            }

            private void buildPatterns(TrieNode node,
                                       StringBuilder current,
                                       int depth,
                                       List<String> results) {

                if (node.children.isEmpty()) {
                    results.add(current.toString());
                    return;
                }

                if (node.children.size() == 1) {

                    Map.Entry<String, TrieNode> entry =
                            node.children.entrySet().iterator().next();

                    StringBuilder next = new StringBuilder(current);
                    next.append(":").append(entry.getKey());

                    buildPatterns(entry.getValue(), next, depth + 1, results);

                } else {

                    String var = "{var" + depth + "}";

                    StringBuilder next = new StringBuilder(current);
                    next.append(":").append(var);

                    TrieNode representative = node.children.values().iterator().next();

                    buildPatterns(representative, next, depth + 1, results);
                }
            }

            // =========================
            // MERGING PHASE (KEY FIX)
            // =========================

            public List<String> mergePatterns(List<String> patterns) {

                List<String> result = new ArrayList<>(patterns);

                boolean changed;

                do {
                    changed = false;

                    for (int i = 0; i < result.size(); i++) {
                        for (int j = i + 1; j < result.size(); j++) {

                            String p1 = result.get(i);
                            String p2 = result.get(j);

                            List<String> t1 = split(p1);
                            List<String> t2 = split(p2);

                            if (isPrefixCompatible(t1, t2)) {

                                String merged = mergeTwo(t1, t2);

                                result.set(i, merged);
                                result.remove(j);

                                changed = true;
                                break;
                            }
                        }
                        if (changed) break;
                    }

                } while (changed);

                return result;
            }

            private List<String> split(String pattern) {
                return Arrays.asList(pattern.split(":"));
            }

            /**
             * Checks if two patterns share a prefix structure allowing merging
             */
            private boolean isPrefixCompatible(List<String> a, List<String> b) {

                int min = Math.min(a.size(), b.size());

                for (int i = 0; i < min; i++) {

                    String t1 = a.get(i);
                    String t2 = b.get(i);

                    if (!t1.equals(t2) &&
                            !t1.startsWith("{var") &&
                            !t2.startsWith("{var")) {
                        return false;
                    }
                }

                return true;
            }

            /**
             * Merge two patterns into a more specific generalized one
             */
            private String mergeTwo(List<String> a, List<String> b) {

                int max = Math.max(a.size(), b.size());
                List<String> merged = new ArrayList<>();

                for (int i = 0; i < max; i++) {

                    String t1 = i < a.size() ? a.get(i) : null;
                    String t2 = i < b.size() ? b.get(i) : null;

                    if (Objects.equals(t1, t2)) {
                        merged.add(t1);
                    } else {
                        merged.add("{var" + i + "}");
                    }
                }

                return String.join(":", merged);
            }
        }
