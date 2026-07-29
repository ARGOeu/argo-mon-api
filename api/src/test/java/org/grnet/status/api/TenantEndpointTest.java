package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.repositories.RoleEndpointRepository;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.ams.PublishResponse;
import org.grnet.status.dtos.downtime.DowntimeRequest;
import org.grnet.status.dtos.downtime.DowntimeResponse;
import org.grnet.status.dtos.downtime.DowntimeServiceEndpointRequest;
import org.grnet.status.dtos.general.ExistResponseDto;
import org.grnet.status.dtos.incident.*;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.tenant.ContactDto;
import org.grnet.status.dtos.tenant.TenantInfoDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.dtos.topology.EndpointTopologyDto;
import org.grnet.status.dtos.topology.FeedTopologyDto;
import org.grnet.status.dtos.topology.WebApiEndpointTopologyResponse;
import org.grnet.status.dtos.topology.WebApiFeedsTopologyResponse;
import org.grnet.status.enums.DowntimeSeverity;
import org.grnet.status.enums.FeedType;
import org.grnet.status.enums.IncidentStatus;
import org.grnet.status.services.clients.AmsClient;
import org.grnet.status.services.clients.AmsClientFactory;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
@QuarkusTestResource(KeycloakComposeResource.class)
public class TenantEndpointTest extends KeycloakTest {

    @InjectMock
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @InjectMock
    AmsClientFactory amsClientFactory;

    @Inject
    TestEntitlementProvider entitlementProvider;

    @Inject
    RoleEndpointRepository roleEndpointRepository;

    private String currentMockId;

    @BeforeEach
    public void mockArgoClient() throws Exception {
        when(argoWebApiClient.createTenant(any(), any())).thenAnswer(invocation -> loadMockTenantResponse(currentMockId));
        when(argoWebApiClient.getTenant(any(), any())).thenAnswer(invocation -> loadMockTenantGetResponse(currentMockId));
    }

    @BeforeEach
    void setupRepo() {
        var testRepo = new TestRoleEndpointRepository();
        QuarkusMock.installMockForType(testRepo, RoleEndpointRepository.class);
        this.roleEndpointRepository = testRepo;
    }

    @BeforeEach
    void reset() {
        entitlementProvider.reset();
        ((TestRoleEndpointRepository) roleEndpointRepository).reset();
    }

    @BeforeEach
    public void cleanUp() {
        tenantService.deleteAll();
    }

    @BeforeEach
    void mockAms() {
        var mockClient = mock(AmsClient.class);
        when(amsClientFactory.buildClient(anyString())).thenReturn(mockClient);

        var response = new PublishResponse();
        response.setMessageIds(List.of("mock-msg"));

        when(mockClient.publish(anyString(), anyString(), anyString(), any())).thenReturn(response);
    }

    private void mockSuperAdmin() {
        entitlementProvider.setSuperAdmin(true);
        entitlementProvider.setEntitlements(List.of());
    }

    private void mockTenantAdmin() {
        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(entitlement(currentMockId, "tenant_admin")));
    }

    private void mockTenantViewer() {
        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(entitlement(currentMockId, "tenant_viewer")));
    }

    private void mockIncidentAdmin() {
        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(entitlement(currentMockId, "incident_admin")));
    }

    private Entitlement entitlement(String tenantId, String role) {
        String raw = "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:"
                + role + ":TENANT:" + tenantId + ":role=member";

        return new Entitlement(
                "status-pages",
                List.of("status-pages", role, "TENANT", tenantId),
                role,
                raw
        );
    }

    private void mockRoleEndpoints(String role, String... endpoints) {
        var roleEndpoints = new ArrayList<RoleEndpoint>();

        for (int i = 0; i < endpoints.length; i++) {
            roleEndpoints.add(new RoleEndpoint(
                    (long) i + 1,
                    role,
                    role,
                    endpoints[i],
                    LocalDateTime.now(),
                    null
            ));
        }

        ((TestRoleEndpointRepository) roleEndpointRepository).set(roleEndpoints);
    }

    private void mockRoleEndpointsWithScope(String role, String scope, String... endpoints) {
        var roleEndpoints = new ArrayList<RoleEndpoint>();

        for (int i = 0; i < endpoints.length; i++) {
            roleEndpoints.add(new RoleEndpoint(
                    (long) i + 1,
                    role,
                    role,
                    endpoints[i],
                    LocalDateTime.now(),
                    scope
            ));
        }

        ((TestRoleEndpointRepository) roleEndpointRepository).set(roleEndpoints);
    }

    private void mockArgoClientFeed(FeedType feedType) {
        var response = new WebApiFeedsTopologyResponse();
        var feedDto = new FeedTopologyDto();
        feedDto.type = feedType;
        response.data = List.of(feedDto);

        var status = new Status();
        status.setMessage("test feed");
        status.setCode("200");

        when(argoWebApiClient.getFeedTopology(any(), any())).thenReturn(response);
    }

    private void mockTopologyEndpoints(List<EndpointTopologyDto> endpoints) {
        var response = new WebApiEndpointTopologyResponse();
        response.data = endpoints;

        Mockito.when(argoWebApiClient.fetchTopologyEndpointsSuperAdmin(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
        )).thenReturn(response);
    }

    @Test
    public void getTenant() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();

        var tenant = createTenant();

        mockRoleEndpoints("tenant_admin", "GET_/v1/tenants/{id}");

        var result = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .get("/v1/tenants/{id}", tenant.id)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        assertEquals(tenant.info.name, result.info.name);
    }

    @Test
    public void updateTenant() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();

        var request = createTenant();

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        var tenantContact1 = new ContactDto();

        tenantInfo1.name = "LOCALTENANT";
        tenantInfo1.email = "test2-updated@gmail.com";
        tenantInfo1.description = "this is test2 updated tenant description";
        tenantInfo1.image = "https://example/image.png";
        tenantInfo1.website = "https://test2.updated.tenant.org";

        tenantContact1.name = "Test User";
        tenantContact1.email = "test@gmail.com";
        tenantContact1.type = "ADMIN";

        request1.info = tenantInfo1;
        request1.contacts = Collections.singletonList(tenantContact1);

        var response1 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request1)
                .when()
                .put("/v1/tenants/{id}", request.id)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        assertEquals("LOCALTENANT", response1.info.name);
    }

    @Test
    public void updateNotExistingTenant() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();

        var request = new TenantRequestDto();
        var info = new TenantInfoDto();

        info.name = "NOT_EXIST";
        info.email = "test@test.com";
        request.info = info;

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .put("/v1/tenants/{id}", currentMockId)
                .then()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);

        assertTrue(response.message.contains(currentMockId));
    }

    @Test
    public void updateTenantForbiddenUser() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();

        var tenant = createTenant();

        currentMockId = tenant.id;
        mockTenantViewer();

        var request = new TenantRequestDto();
        request.info = new TenantInfoDto();

        given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request)
                .put("/v1/tenants/{id}", tenant.id)
                .then()
                .statusCode(403);
    }

    @Test
    public void viewTenants() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();
        mockTenantViewer();

        mockRoleEndpoints("tenant_viewer", "GET_/v1/tenants");

        createTenant();

        var list = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .get("/v1/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertEquals(0, list.getContent().size());
    }

    @Test
    public void testGetProjectsByTenant() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();

        var tenant = createTenant();

        var project = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(buildProject())
                .post("/v1/admin/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var assign = new TenantProjectRequestDto();
        assign.tenantId = tenant.id;
        assign.projectIds = List.of(project.id);

        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(assign)
                .put("/v1/admin/tenant-project")
                .then()
                .statusCode(200);

        var result = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .get("/v1/tenants/" + tenant.id + "/projects")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertEquals(1, result.getContent().size());
    }

    @Test
    public void notExistingTenant() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();

        given()
                .auth().oauth2(adminToken)
                .get("/tenants/{id}", currentMockId)
                .then()
                .statusCode(404);
    }

    @Test
    public void checkSlugNotExists() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();

        var tenant = createTenant();

        currentMockId = tenant.id;
        mockTenantViewer();

        mockRoleEndpoints(
                "tenant_viewer",
                "GET_/v1/tenants/{id}/pages/check-slug/{slug}"
        );

        var response = given()
                .auth().oauth2(tenantViewer)
                .get("/v1/tenants/{id}/pages/check-slug/{slug}", tenant.id, "slug")
                .then()
                .statusCode(200)
                .extract()
                .as(ExistResponseDto.class);

        assertFalse(response.exist);
    }

    @Test
    public void createIncident() {
        var tenant = setupTenantAdmin();

        mockRoleEndpoints(
                "tenant_admin",
                "POST_/v1/tenants/{id}/incidents"
        );

        var response = createIncident(tenant.id);

        assertNotNull(response.id);
        assertNotNull(response.incidentNumber);
        assertTrue(response.incidentNumber.matches("INC-\\d{4}-\\d{6,}"));
        assertEquals("ESHOP unavailable", response.title);
        assertEquals("Users cannot access the ESHOP service.", response.description);
        assertEquals(IncidentStatus.NEW, response.status);
        assertNotNull(response.createdBy);
        assertNotNull(response.service);
        assertEquals("6a6e8037-1e23-4b65-a75a-37d9e8d5bc44", response.service.id);
        assertEquals("ESHOP", response.service.name);
        assertNotNull(response.createdAt);
        assertNotNull(response.updatedAt);
    }

    @Test
    public void updateIncidentStatus() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();

        var tenant = createTenant();

        currentMockId = tenant.id;

        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(entitlement(currentMockId, "incident_admin")));

        mockRoleEndpointsWithScope(
                "incident_admin",
                "ALL",
                "POST_/v1/tenants/{id}/incidents",
                "PATCH_/v1/tenants/{id}/incidents/{incident-id}/status"
        );

        var incident = createIncident(tenant.id);

        var response = updateIncidentStatus(tenant.id, incident.id, IncidentStatus.ASSIGNED);

        assertEquals(incident.id, response.id);
        assertEquals(incident.incidentNumber, response.incidentNumber);
        assertEquals(IncidentStatus.ASSIGNED, response.status);
    }

    @Test
    public void createIncidentComment() {
        var tenant = setupTenantAdmin();

        mockRoleEndpoints(
                "tenant_admin",
                "POST_/v1/tenants/{id}/incidents",
                "POST_/v1/tenants/{id}/incidents/{incident-id}/comments"
        );

        var incident = createIncident(tenant.id);

        var commentRequest = new IncidentCommentRequestDto();
        commentRequest.comment = "The service owner has been contacted.";

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(commentRequest)
                .post("/v1/tenants/{id}/incidents/{incident-id}/comments", tenant.id, incident.id)
                .then()
                .statusCode(201)
                .extract()
                .as(IncidentResponseDto.class);

        assertEquals(incident.id, response.id);
        assertEquals(IncidentStatus.NEW, response.status);
        assertNotNull(response.comments);
        assertEquals(1, response.comments.size());

        var comment = response.comments.get(0);

        assertNotNull(comment.id);
        assertEquals("The service owner has been contacted.", comment.comment);
    }

    @Test
    public void getAllIncidents() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();

        var tenant = createTenant();

        currentMockId = tenant.id;

        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(
                List.of(entitlement(currentMockId, "incident_admin"))
        );

        mockRoleEndpointsWithScope(
                "incident_admin",
                "ALL",
                "POST_/v1/tenants/{id}/incidents",
                "GET_/v1/tenants/{id}/incidents"
        );

        createIncident(tenant.id);

        var secondRequest = buildIncidentRequest(
                "FORUM degraded",
                "Users experience delays in the FORUM service.",
                "4e4f96be-a7a4-41b7-b765-d003e421ab44",
                "FORUM"
        );

        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(secondRequest)
                .post("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(201);

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .queryParam("page", 1)
                .queryParam("size", 10)
                .get("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertEquals(2, response.getTotalElements());
    }

    @Test
    public void getIncident() {
        var tenant = setupTenantAdmin();

        mockRoleEndpoints(
                "tenant_admin",
                "POST_/v1/tenants/{id}/incidents",
                "GET_/v1/tenants/{id}/incidents/{incident-id}"
        );

        var incident = createIncident(tenant.id);

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .get("/v1/tenants/{id}/incidents/{incident-id}", tenant.id, incident.id)
                .then()
                .statusCode(200)
                .extract()
                .as(IncidentResponseDto.class);

        assertEquals(incident.id, response.id);
        assertEquals(incident.incidentNumber, response.incidentNumber);
        assertEquals("ESHOP unavailable", response.title);
        assertEquals("Users cannot access the ESHOP service.", response.description);
        assertEquals(IncidentStatus.NEW, response.status);
        assertNotNull(response.createdBy);
        assertNotNull(response.service);
        assertEquals("6a6e8037-1e23-4b65-a75a-37d9e8d5bc44", response.service.id);
        assertEquals("ESHOP", response.service.name);
        assertNotNull(response.createdAt);
        assertNotNull(response.updatedAt);
    }

    @Test
    public void getIncidentActivity() {
        var tenant = setupTenantAdmin();

        mockRoleEndpointsWithScope(
                "tenant_admin",
                "ALL",
                "POST_/v1/tenants/{id}/incidents",
                "PATCH_/v1/tenants/{id}/incidents/{incident-id}/status",
                "GET_/v1/tenants/{id}/incidents/{incident-id}/activity"
        );

        var incident = createIncident(tenant.id);

        updateIncidentStatus(tenant.id, incident.id, IncidentStatus.ASSIGNED);
        updateIncidentStatus(tenant.id, incident.id, IncidentStatus.IN_PROGRESS);

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .get("/v1/tenants/{id}/incidents/{incident-id}/activity", tenant.id, incident.id)
                .then()
                .statusCode(200)
                .extract()
                .as(IncidentActivityResponseDto[].class);

        assertEquals(2, response.length);
        assertEquals(IncidentStatus.NEW, response[0].previousStatus);
        assertEquals(IncidentStatus.ASSIGNED, response[0].newStatus);
        assertEquals("admin", response[1].changedBy);
        assertNotNull(response[1].createdAt);
    }

    @Test
    public void testCreateDowntimeNotExistingTopologyItems() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();
        mockArgoClientFeed(FeedType.DESY_MARKETPLACE);

        mockTopologyEndpoints(List.of(
                endpointSample("service3", "host1.example.org"),
                endpointSample("service2", "host3.example.org")
        ));

        var tenant = createTenant();
        var request = buildCreateDowntime();

        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateDowntime() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();
        mockArgoClientFeed(FeedType.DESY_MARKETPLACE);

        mockTopologyEndpoints(List.of(
                endpointSample("service1", "host1.example.org"),
                endpointSample("service2", "host2.example.org")
        ));

        var tenant = createTenant();
        var request = buildCreateDowntime();

        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(200)
                .extract()
                .as(DowntimeResponse.class);

        assertNotNull(created.getId());
        assertEquals(2, created.getServices().size());
    }

    @Test
    public void testCreateDowntimeForbiddenFeed() {
        currentMockId = UUID.randomUUID().toString();

        mockArgoClientFeed(FeedType.EXTERNAL);

        mockTopologyEndpoints(List.of(
                endpointSample("service1", "host1.example.org"),
                endpointSample("service2", "host2.example.org")
        ));

        mockSuperAdmin();

        var tenant = createTenant();
        var request = buildCreateDowntime();

        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(403);
    }

    @Test
    public void testCreateDowntimeNonFeed() {
        currentMockId = UUID.randomUUID().toString();

        mockTopologyEndpoints(List.of(
                endpointSample("service1", "host1.example.org"),
                endpointSample("service2", "host2.example.org")
        ));

        mockSuperAdmin();

        var tenant = createTenant();
        var request = buildCreateDowntime();

        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(403);
    }

    @Test
    public void testUpdateDowntime() {
        currentMockId = UUID.randomUUID().toString();

        mockArgoClientFeed(FeedType.DESY_MARKETPLACE);

        mockTopologyEndpoints(List.of(
                endpointSample("service1", "host1.example.org"),
                endpointSample("service2", "host2.example.org")
        ));

        mockSuperAdmin();

        var tenant = createTenant();
        var request = buildCreateDowntime();

        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(200)
                .extract()
                .as(DowntimeResponse.class);

        mockTopologyEndpoints(List.of(endpointSample("service6", "hostname6.example.org")));

        var updateRequest = buildUpdateDowntime();

        var updated = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .put("/v1/tenants/{id}/downtimes/{downtime_id}", tenant.id, created.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(DowntimeResponse.class);

        assertEquals(1, updated.getServices().size());
        assertEquals(DowntimeSeverity.Warning.name(), updated.getSeverity());
    }

    private TenantResponseDto createTenant() {
        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        var tenantContact = new ContactDto();

        tenantInfo.name = "LOCALTENANT";
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";

        tenantContact.email = "test@gmail.com";
        tenantContact.name = "Test user";
        tenantContact.type = "ADMIN";

        request.info = tenantInfo;
        request.contacts = Collections.singletonList(tenantContact);

        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .post("/v1/admin/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);
    }

    private TenantResponseDto setupTenantAdmin() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();

        var tenant = createTenant();

        currentMockId = tenant.id;
        mockTenantAdmin();

        return tenant;
    }

    private IncidentRequestDto buildIncidentRequest() {
        return buildIncidentRequest(
                "ESHOP unavailable",
                "Users cannot access the ESHOP service.",
                "6a6e8037-1e23-4b65-a75a-37d9e8d5bc44",
                "ESHOP"
        );
    }

    private IncidentRequestDto buildIncidentRequest(
            String title,
            String description,
            String serviceId,
            String serviceName
    ) {
        var request = new IncidentRequestDto();

        request.title = title;
        request.description = description;
        request.service = new ServiceDto();
        request.service.id = serviceId;
        request.service.name = serviceName;

        return request;
    }

    private IncidentResponseDto createIncident(String tenantId) {
        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(buildIncidentRequest())
                .post("/v1/tenants/{id}/incidents", tenantId)
                .then()
                .statusCode(201)
                .extract()
                .as(IncidentResponseDto.class);
    }

    private IncidentResponseDto updateIncidentStatus(
            String tenantId,
            String incidentId,
            IncidentStatus status
    ) {
        var request = new IncidentUpdateRequestDto();
        request.status = status;

        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .patch("/v1/tenants/{id}/incidents/{incident-id}/status", tenantId, incidentId)
                .then()
                .statusCode(200)
                .extract()
                .as(IncidentResponseDto.class);
    }

    private ProjectRequestDto buildProject() {
        var dto = new ProjectRequestDto();
        dto.name = "Project-" + UUID.randomUUID();
        dto.description = "desc";
        dto.startDate = Timestamp.from(Instant.now());
        dto.endDate = Timestamp.from(Instant.now());
        return dto;
    }

    private TenantWebApiCreateResponse loadMockTenantResponse(String id) {
        var response = new TenantWebApiCreateResponse();
        var data = new TenantWebApiCreateResponse.Data();

        data.setId(id);
        response.setData(data);

        return response;
    }

    private TenantWebApiGetResponse loadMockTenantGetResponse(String id) {
        var response = new TenantWebApiGetResponse();
        var data = new TenantWebApiGetResponse.Data();

        data.setId(id);

        var info = new TenantWebApiGetResponse.Info();
        info.setName("LOCALTENANT");
        data.setInfo(info);

        var dbConf = new TenantWebApiGetResponse.DbConf();
        dbConf.setPort(80);
        dbConf.setDatabase("mydb");
        dbConf.setPassword("password");
        dbConf.setServer("server1.test.org");
        dbConf.setStore("store");
        dbConf.setUsername("username");

        var list = new ArrayList<TenantWebApiGetResponse.DbConf>();
        list.add(dbConf);

        data.setDb_conf(list);
        response.setData(List.of(data));

        return response;
    }

    private DowntimeRequest buildCreateDowntime() {
        var dto = new DowntimeRequest();

        dto.setName("Test Downtime ");
        dto.setMessage("This is a test downtime");
        dto.setSeverity("Outage");
        dto.setScheduledAt(Instant.parse("2025-10-22T12:44:48.107Z"));
        dto.setCompletedAt(Instant.parse("2025-10-22T12:44:48.107Z"));

        var service1 = new DowntimeServiceEndpointRequest();
        service1.setHostname("host1.example.org");
        service1.setService("service1");

        var service2 = new DowntimeServiceEndpointRequest();
        service2.setHostname("host2.example.org");
        service2.setService("service2");

        dto.setServices(List.of(service1, service2));

        return dto;
    }

    private DowntimeRequest buildUpdateDowntime() {
        var dto = new DowntimeRequest();

        dto.setName("Test Downtime Updated");
        dto.setMessage("This is a test downtime updated");
        dto.setSeverity("Warning");
        dto.setScheduledAt(Instant.parse("2025-01-22T12:44:48.107Z"));
        dto.setCompletedAt(Instant.parse("2025-01-22T12:44:48.107Z"));

        var service = new DowntimeServiceEndpointRequest();
        service.setHostname("hostname6.example.org");
        service.setService("service6");

        dto.setServices(List.of(service));

        return dto;
    }

    private EndpointTopologyDto endpointSample(String service, String hostname) {
        var dto = new EndpointTopologyDto();
        dto.setService(service);
        dto.setHostname(hostname);
        return dto;
    }
}
