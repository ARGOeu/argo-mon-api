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
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.downtime.DowntimeRequest;
import org.grnet.status.dtos.downtime.DowntimeResponse;
import org.grnet.status.dtos.downtime.DowntimeServiceEndpointRequest;
import org.grnet.status.dtos.general.ExistResponseDto;
import org.grnet.status.dtos.InformativeResponse;
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
import org.grnet.status.dtos.topology.FeedTopologyDto;
import org.grnet.status.dtos.topology.WebApiFeedsTopologyResponse;
import org.grnet.status.enums.DowntimeSeverity;
import org.grnet.status.enums.IncidentStatus;
import org.grnet.status.dtos.topology.EndpointTopologyDto;
import org.grnet.status.dtos.topology.WebApiEndpointTopologyResponse;
import org.grnet.status.enums.FeedType;
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

    private FeedType feedType;

    @BeforeEach
    public void mockArgoClient() throws Exception {
        when(argoWebApiClient.createTenant(any(), any())).thenAnswer(invocation -> loadMockTenantResponse(currentMockId));
        when(argoWebApiClient.getTenant(any(), any())).thenAnswer(invocation -> loadMockTenantGetResponse(currentMockId));

    }

    private void mockArgoClientFeed(FeedType feedType) {
        var response = new WebApiFeedsTopologyResponse();
        var feedDto = new FeedTopologyDto();
        feedDto.type = feedType;
        response.data = List.of(feedDto);
        var status = new Status();
        status.setMessage("test feed");
        status.setCode("200");
        when(argoWebApiClient.getFeedTopology(any(), any()))
                .thenReturn(response);
    }

    private void mockTopologyEndpoints(List<EndpointTopologyDto> endpoints) {
        WebApiEndpointTopologyResponse response = new WebApiEndpointTopologyResponse();
        response.data = endpoints;

        Mockito.when(argoWebApiClient.fetchTopologyEndpointsSuperAdmin(
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyString()))
                .thenReturn(response);
    }

    // -------------------------------------------------------------------------
    // SETUP ROLE REPOSITORY
    // -------------------------------------------------------------------------
    @BeforeEach
    void setupRepo() {
        TestRoleEndpointRepository testRepo = new TestRoleEndpointRepository();
        QuarkusMock.installMockForType(testRepo, RoleEndpointRepository.class);
        this.roleEndpointRepository = testRepo;
    }

    // -------------------------------------------------------------------------
    // RESET STATE
    // -------------------------------------------------------------------------
    @BeforeEach
    void reset() {
        entitlementProvider.reset();
        ((TestRoleEndpointRepository) roleEndpointRepository).reset();
    }

    @BeforeEach
    public void cleanUp() {
        tenantService.deleteAll();
    }


    // -------------------------------------------------------------------------
    // ENTITLEMENTS
    // -------------------------------------------------------------------------
    private void mockSuperAdmin() {
        entitlementProvider.setSuperAdmin(true);
        entitlementProvider.setEntitlements(List.of());
    }

    private void mockTenantAdmin() {
        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(
                entitlement(currentMockId, "tenant_admin")
        ));
    }

    private void mockTenantViewer() {
        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(
                entitlement(currentMockId, "tenant_viewer")
        ));
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

    // -------------------------------------------------------------------------
    // AMS MOCK
    // -------------------------------------------------------------------------
    @BeforeEach
    void mockAms() {
        var mockClient = mock(org.grnet.status.services.clients.AmsClient.class);

        when(amsClientFactory.buildClient(anyString()))
                .thenReturn(mockClient);

        var resp = new org.grnet.status.dtos.ams.PublishResponse();
        resp.setMessageIds(List.of("mock-msg"));

        when(mockClient.publish(anyString(), anyString(), anyString(), any()))
                .thenReturn(resp);
    }

    // -------------------------------------------------------------------------
    // TESTS (UNCHANGED LOGIC, ONLY FIXED CONTEXT ORDERING)
    // -------------------------------------------------------------------------

    @Test
    public void getTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        // IMPORTANT: allow interceptor access
        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "GET_/v1/tenants/{id}",
                        LocalDateTime.now(),
                        null
                )
        ));

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
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

        //var webApi = new ArgoWebApiRequest();

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
                .contentType(ContentType.JSON)
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

        var req = new TenantRequestDto();
        var info = new TenantInfoDto();
        info.name = "NOT_EXIST";
        info.email = "test@test.com";
        req.info = info;

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
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

        var tenant = createTenant("LOCALTENANT");
        mockTenantViewer();

        var req = new TenantRequestDto();
        req.info = new TenantInfoDto();

        given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(req)
                .put("/v1/tenants/{id}", tenant.id)
                .then()
                .statusCode(403);
    }

    @Test
    public void viewTenants() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        mockSuperAdmin();
        mockTenantViewer();

        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_viewer",
                        "tenant_viewer",
                        "GET_/v1/tenants",
                        LocalDateTime.now(),
                        null
                )
        ));

        var tenant = createTenant("LOCALTENANT");

        var list = given()
                .auth()
                .oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .get("/v1/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertEquals(0, list.getContent().size());
    }


    //    @Test
//    public void viewTenants() {
//
//        currentMockId = UUID.randomUUID().toString();
//
//        mockSuperAdmin();
//        mockTenantViewer();
//
//        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
//                new RoleEndpoint(
//                        1L,
//                        "tenant_viewer",
//                        "tenant_viewer",
//                        "GET_/v1/tenants",
//                        LocalDateTime.now()
//                )
//        ));
//
//        // FIX: unique tenant to avoid duplicate error
//        var tenantName = "LOCALTENANT-" + UUID.randomUUID();
//        createTenant(tenantName);
//
//        var list = given()
//                .auth().oauth2(tenantViewer)
//                .contentType(ContentType.JSON)
//                .get("/v1/tenants")
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(PageResource.class);
//
//        assertEquals(1, list.getContent().size());
//    }
    @Test
    public void testGetProjectsByTenant() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

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

//    @Test
//    public void fetchReports() {
//
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
//        mockSuperAdmin();
//        var tenant = createTenant("LOCALTENANT");
//
//        mockTenantViewer();
//
//        // IMPORTANT: mock interceptor role endpoint lookup
//        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
//                new RoleEndpoint(
//                        1L,
//                        "tenant_viewer",
//                        "tenant_viewer",
//                        "GET_/v1/tenants/{id}/reports",
//                        LocalDateTime.now()
//                )
//        ));
//
//        var reports = given()
//                .auth()
//                .oauth2(tenantViewer)
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/v1/tenants/{id}/reports", tenant.id)
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(PartialReportResponseDto[].class);
//
//        assertNotNull(reports);
//        assertTrue(reports.length > 0);
//    }

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

        var tenant = createTenant("LOCALTENANT");

        mockTenantViewer();

        // IMPORTANT: mock interceptor role endpoint lookup
        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_viewer",
                        "tenant_viewer",
                        "GET_/v1/tenants/{id}/pages/check-slug/{slug}",
                        LocalDateTime.now(),
                        null
                )
        ));

        var resp = given()
                .auth().oauth2(tenantViewer)
                .get("/v1/tenants/{id}/pages/check-slug/{slug}", tenant.id, "slug")
                .then()
                .statusCode(200)
                .extract()
                .as(ExistResponseDto.class);

        assertFalse(resp.exist);
    }

    @Test
    public void createIncident() {

        currentMockId = "42c1152d-e23c-4a19-b51a-b27f1eb7f37f";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        currentMockId = tenant.id;
        mockTenantAdmin();

        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/incidents",
                        LocalDateTime.now(),
                        null
                )
        ));

        var request = new IncidentRequestDto();
        request.title = "ESHOP unavailable";
        request.description = "Users cannot access the ESHOP service.";

        request.service = new ServiceDto();
        request.service.id = "6a6e8037-1e23-4b65-a75a-37d9e8d5bc44";
        request.service.name = "ESHOP";

        var response = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(IncidentResponseDto.class);

        assertNotNull(response.id);
        assertNotNull(response.incidentNumber);
        assertTrue(response.incidentNumber.matches("INC-\\d{4}-\\d{6,}"));

        assertEquals("ESHOP unavailable", response.title);
        assertEquals("Users cannot access the ESHOP service.", response.description);

        assertEquals(IncidentStatus.REPORTED, response.status);

        assertNotNull(response.createdBy);

        assertNotNull(response.service);
        assertEquals("6a6e8037-1e23-4b65-a75a-37d9e8d5bc44", response.service.id);
        assertEquals("ESHOP", response.service.name);

        assertNotNull(response.createdAt);
        assertNotNull(response.updatedAt);
    }

    @Test
    public void updateIncidentStatus() {

        currentMockId = "42c1152d-e23c-4a19-b51a-b27f1eb7f37f";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        currentMockId = tenant.id;
        mockTenantAdmin();

        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/incidents",
                        LocalDateTime.now(),
                        null
                ),
                new RoleEndpoint(
                        2L,
                        "tenant_admin",
                        "tenant_admin",
                        "PATCH_/v1/tenants/{id}/incidents/{incident_id}/status",
                        LocalDateTime.now(),
                        null
                )
        ));

        var createRequest = new IncidentRequestDto();
        createRequest.title = "ESHOP unavailable";
        createRequest.description = "Users cannot access the ESHOP service.";

        createRequest.service = new ServiceDto();
        createRequest.service.id = "6a6e8037-1e23-4b65-a75a-37d9e8d5bc44";
        createRequest.service.name = "ESHOP";

        var incident = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(IncidentResponseDto.class);

        var updateRequest = new IncidentUpdateRequestDto();
        updateRequest.status = IncidentStatus.INVESTIGATING;

        var response = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .patch("/v1/tenants/{id}/incidents/{incident_id}/status", tenant.id, incident.id)
                .then()
                .statusCode(200)
                .extract()
                .as(IncidentResponseDto.class);

        assertEquals(incident.id, response.id);
        assertEquals(incident.incidentNumber, response.incidentNumber);
        assertEquals(IncidentStatus.INVESTIGATING, response.status);
    }

    @Test
    public void createIncidentComment() {

        currentMockId = "42c1152d-e23c-4a19-b51a-b27f1eb7f37f";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        currentMockId = tenant.id;
        mockTenantAdmin();

        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/incidents",
                        LocalDateTime.now(),
                        null
                ),
                new RoleEndpoint(
                        2L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/incidents/{incident_id}/comments",
                        LocalDateTime.now(),
                        null
                )
        ));

        var createRequest = new IncidentRequestDto();
        createRequest.title = "ESHOP unavailable";
        createRequest.description =
                "Users cannot access the ESHOP service.";

        createRequest.service = new ServiceDto();
        createRequest.service.id =
                "6a6e8037-1e23-4b65-a75a-37d9e8d5bc44";
        createRequest.service.name = "ESHOP";

        var incident = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(IncidentResponseDto.class);

        var commentRequest = new IncidentCommentRequestDto();
        commentRequest.comment = "The service owner has been contacted.";

        var response = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(commentRequest)
                .when()
                .post("/v1/tenants/{id}/incidents/{incident_id}/comments", tenant.id, incident.id)
                .then()
                .statusCode(201)
                .extract()
                .as(IncidentResponseDto.class);

        assertEquals(incident.id, response.id);
        assertEquals(IncidentStatus.REPORTED, response.status);

        assertNotNull(response.comments);
        assertEquals(1, response.comments.size());

        var comment = response.comments.get(0);

        assertNotNull(comment.id);
        assertEquals("The service owner has been contacted.", comment.comment);

    }


    @Test
    public void getAllIncidents() {

        currentMockId = "42c1152d-e23c-4a19-b51a-b27f1eb7f37f";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        currentMockId = tenant.id;
        mockTenantAdmin();

        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/incidents",
                        LocalDateTime.now(),
                        null
                ),
                new RoleEndpoint(
                        2L,
                        "tenant_admin",
                        "tenant_admin",
                        "GET_/v1/tenants/{id}/incidents",
                        LocalDateTime.now(),
                        null
                )
        ));

        var firstRequest = new IncidentRequestDto();
        firstRequest.title = "ESHOP unavailable";
        firstRequest.description = "Users cannot access the ESHOP service.";

        firstRequest.service = new ServiceDto();
        firstRequest.service.id = "6a6e8037-1e23-4b65-a75a-37d9e8d5bc44";
        firstRequest.service.name = "ESHOP";

        given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(firstRequest)
                .when()
                .post("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(201);

        var secondRequest = new IncidentRequestDto();
        secondRequest.title = "FORUM degraded";
        secondRequest.description = "Users experience delays in the FORUM service.";

        secondRequest.service = new ServiceDto();
        secondRequest.service.id = "4e4f96be-a7a4-41b7-b765-d003e421ab44";
        secondRequest.service.name = "FORUM";

        given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(secondRequest)
                .when()
                .post("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(201);

        var response = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .queryParam("page", 1)
                .queryParam("size", 10)
                .when()
                .get("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);


        assertEquals(2, response.getTotalElements());
    }

    @Test
    public void getIncident() {

        currentMockId = "42c1152d-e23c-4a19-b51a-b27f1eb7f37f";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        currentMockId = tenant.id;
        mockTenantAdmin();

        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/incidents",
                        LocalDateTime.now(),
                        null
                ),
                new RoleEndpoint(
                        2L,
                        "tenant_admin",
                        "tenant_admin",
                        "GET_/v1/tenants/{id}/incidents/{incident_id}",
                        LocalDateTime.now(),
                        null
                )
        ));

        var createRequest = new IncidentRequestDto();
        createRequest.title = "ESHOP unavailable";
        createRequest.description = "Users cannot access the ESHOP service.";

        createRequest.service = new ServiceDto();
        createRequest.service.id =
                "6a6e8037-1e23-4b65-a75a-37d9e8d5bc44";
        createRequest.service.name = "ESHOP";

        var incident = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/v1/tenants/{id}/incidents", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(IncidentResponseDto.class);

        var response = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get(
                        "/v1/tenants/{id}/incidents/{incident_id}",
                        tenant.id,
                        incident.id
                )
                .then()
                .statusCode(200)
                .extract()
                .as(IncidentResponseDto.class);

        assertEquals(incident.id, response.id);
        assertEquals(incident.incidentNumber, response.incidentNumber);
        assertEquals("ESHOP unavailable", response.title);
        assertEquals(
                "Users cannot access the ESHOP service.",
                response.description
        );

        assertEquals(IncidentStatus.REPORTED, response.status);
        assertNotNull(response.createdBy);

        assertNotNull(response.service);
        assertEquals(
                "6a6e8037-1e23-4b65-a75a-37d9e8d5bc44",
                response.service.id
        );
        assertEquals("ESHOP", response.service.name);

        assertNotNull(response.createdAt);
        assertNotNull(response.updatedAt);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------
    private TenantResponseDto createTenant(String tenantName) {
        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        var tenantContact = new ContactDto();
        tenantInfo.name = tenantName;
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
                .when()
                .post("/v1/admin/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);
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
        var r = new TenantWebApiCreateResponse();
        var d = new TenantWebApiCreateResponse.Data();
        d.setId(id);
        r.setData(d);
        return r;
    }

    private TenantWebApiGetResponse loadMockTenantGetResponse(String id) {
        var r = new TenantWebApiGetResponse();
        var d = new TenantWebApiGetResponse.Data();
        d.setId(id);

        var info = new TenantWebApiGetResponse.Info();
        info.setName("LOCALTENANT");
        d.setInfo(info);
        var dbConf=new TenantWebApiGetResponse.DbConf();
        dbConf.setPort(80);
        dbConf.setDatabase("mydb");
        dbConf.setPassword("password");
        dbConf.setServer("server1.test.org");
        dbConf.setStore("store");
        dbConf.setUsername("username");
        var list=new ArrayList<TenantWebApiGetResponse.DbConf>();
        list.add(dbConf);
        d.setDb_conf(list);

        r.setData(List.of(d));
        return r;
    }


    @Test
    public void testCreateDowntimeNotExistingTopologyItems() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();
        mockArgoClientFeed(FeedType.DESY_MARKETPLACE);

        mockTopologyEndpoints(List.of(
                EndpointSample("service3", "host1.example.org"),
                EndpointSample("service2", "host3.example.org")
        ));
        var tenant = createTenant("LOCALTENANT");

        var req = buildCreateDowntime();

        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateDowntime() {
        currentMockId = UUID.randomUUID().toString();

        mockSuperAdmin();
        mockArgoClientFeed(FeedType.DESY_MARKETPLACE);
  //      mockTenantInitialized(tenant.id);

        mockTopologyEndpoints(List.of(
                EndpointSample("service1", "host1.example.org"),
                EndpointSample("service2", "host2.example.org")
        ));
        var tenant = createTenant("LOCALTENANT");

        var req = buildCreateDowntime();

        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
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
                EndpointSample("service1", "host1.example.org"),
                EndpointSample("service2", "host2.example.org")
        ));

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        var req = buildCreateDowntime();

        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(403);

    }

    @Test
    public void testCreateDowntimeNonFeed() {
        currentMockId = UUID.randomUUID().toString();
        mockTopologyEndpoints(List.of(
                EndpointSample("service1", "host1.example.org"),
                EndpointSample("service2", "host2.example.org")
        ));

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");


        var req = buildCreateDowntime();

        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(403);

    }

    @Test
    public void testUpdateDowntime() {
        currentMockId = UUID.randomUUID().toString();
        mockArgoClientFeed(FeedType.DESY_MARKETPLACE);

        mockTopologyEndpoints(List.of(
                EndpointSample("service1", "host1.example.org"),
                EndpointSample("service2", "host2.example.org")
        ));

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");
        var req = buildCreateDowntime();


        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/v1/tenants/{id}/downtimes", tenant.id)
                .then()
                .statusCode(200)
                .extract()
                .as(DowntimeResponse.class);

        mockTopologyEndpoints(List.of(
                EndpointSample("service6", "hostname6.example.org")
        ));

        var req2 = buildUpdateDowntime();

        var updated = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req2)
                .when()
                .put("/v1/tenants/{id}/downtimes/{downtime_id}", tenant.id, created.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(DowntimeResponse.class);

        assertEquals(1, updated.getServices().size());

        assertEquals(DowntimeSeverity.Warning.name(), updated.getSeverity());
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

        var list = new ArrayList<DowntimeServiceEndpointRequest>();
        list.add(service1);
        list.add(service2);
        dto.setServices(list);
        return dto;
    }

    private FeedTopologyDto buildFeedType(FeedType type) {

        var dto = new FeedTopologyDto();
        dto.type = type;
        dto.feedUrl = "https://example.com";

        return dto;
    }


    private DowntimeRequest buildUpdateDowntime() {

        var dto = new DowntimeRequest();
        dto.setName("Test Downtime Updated");
        dto.setMessage("This is a test downtime updated");
        dto.setSeverity("Warning");
        dto.setScheduledAt(Instant.parse("2025-01-22T12:44:48.107Z"));
        dto.setCompletedAt(Instant.parse("2025-01-22T12:44:48.107Z"));

        var service1 = new DowntimeServiceEndpointRequest();
        service1.setHostname("hostname6.example.org");
        service1.setService("service6");

        var list = new ArrayList<DowntimeServiceEndpointRequest>();
        list.add(service1);
        dto.setServices(list);
        return dto;
    }

    private EndpointTopologyDto EndpointSample(String service, String hostname) {
        EndpointTopologyDto dto = new EndpointTopologyDto();
        dto.setService(service);
        dto.setHostname(hostname);
        return dto;
    }
}