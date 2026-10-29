package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.GroupUserResponse;
import org.grnet.endpoint.scanner.runtime.repositories.RoleEndpointRepository;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.status.api.endpoints.AdminEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.ams.PublishRequest;
import org.grnet.status.dtos.ams.PublishResponse;
import org.grnet.status.dtos.downtime.DowntimeRequest;
import org.grnet.status.dtos.downtime.DowntimeResponse;
import org.grnet.status.dtos.downtime.DowntimeServiceEndpointRequest;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.project.ProjectUpdateDto;
import org.grnet.status.dtos.tenant.ContactDto;
import org.grnet.status.dtos.tenant.TenantInfoDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.status.EventStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusFullResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.enums.DowntimeClassification;
import org.grnet.status.enums.EventStatus;
import org.grnet.status.enums.TenantJobEvent;
import org.grnet.status.enums.resources.TenantResource;
import org.grnet.status.services.GroupManagementService;
import org.grnet.status.services.clients.AmsClient;
import org.grnet.status.services.clients.AmsClientFactory;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@TestHTTPEndpoint(AdminEndpoint.class)
@QuarkusTestResource(KeycloakComposeResource.class)
public class AdminEndpointTest extends KeycloakTest {

    @InjectMock
    GroupManagementService groupManagementService;

    @InjectMock
    AmsClientFactory amsClientFactory;
    @InjectMock
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    private String currentMockId;

    @Inject
    TestEntitlementProvider entitlementProvider;

    @Inject
    RoleEndpointRepository roleEndpointRepository;

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
        String raw =
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:"
                        + role
                        + ":TENANT:"
                        + tenantId
                        + ":role=member";

        return new Entitlement(
                "status-pages",
                List.of("status-pages", role, "TENANT", tenantId),
                role,
                raw
        );
    }

    @BeforeEach
    public void mockArgoClient() throws Exception {

        when(argoWebApiClient.createTenant(any(), any())).thenAnswer(invocation -> {
            // Use the currentMockId set by the test
            return loadMockTenantResponse(currentMockId);
        });

        when(argoWebApiClient.getTenant(any(), any())).thenAnswer(invocation -> {
            // Use the currentMockId set by the test
            return loadMockTenantGetResponse(currentMockId);
        });
    }

    @BeforeEach
    void mockAmsClient() {
        var mockClient = mock(AmsClient.class);

        when(amsClientFactory.buildClient(anyString()))
                .thenReturn(mockClient);

        var resp = new PublishResponse();
        resp.setMessageIds(List.of("mock-message"));

        when(mockClient.publish(anyString(), anyString(), anyString(), any(PublishRequest.class)))
                .thenReturn(resp);
    }

    @BeforeEach
    public void cleanUp() {
        tenantService.deleteAll();
    }


    private TenantWebApiCreateResponse loadMockTenantResponse(String id) {

        var tenantWebApiResponse = new TenantWebApiCreateResponse();
        var data = new TenantWebApiCreateResponse.Data();
        var link = new TenantWebApiCreateResponse.Links();
        var status = new Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        link.setSelf("https://https://test.api.grnet.gr/api/v2/admin/tenants/e1ab046c-8544-47e6-bd8f-e8aa8b83acb3");
        data.setId(id);
        data.setLinks(link);
        tenantWebApiResponse.setData(data);
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }

    private TenantWebApiGetResponse loadMockTenantGetResponse(String id) {

        var tenantWebApiResponse = new TenantWebApiGetResponse();
        tenantWebApiResponse.setData(new ArrayList<>());
        var data = new TenantWebApiGetResponse.Data();
        data.setId(id);
        var info = new TenantWebApiGetResponse.Info();
        info.setCreated("2025-01-01 00:00:00");
        info.setName("TENANT-TEST");
        info.setEmail("test@gmail.com");
        info.setDescription("this is test tenant description");
        info.setImage("https://example/image.png");
        info.setWebsite("https://test.tenant.org");
        info.setUpdated("2025-01-02 00:00:00");
        data.setInfo(info);
        tenantWebApiResponse.getData().add(data);
        data.setInfo(info);
        var status = new Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }

    @Test
    public void createTenant() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

        assertEquals(request.info.name, request.info.name);
        assertEquals(request.performance, Boolean.TRUE);

        assertEquals(currentMockId, request.id);  // check that the id matches what you set
    }


    @Test
    public void deleteTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

        var response1 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .delete("/tenants/{id}", request.id)
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(200, response1.code);
    }


    @Test
    public void deleteTenantForbidden() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

        //var webApi = new ArgoWebApiRequest();
        mockTenantViewer();
        var error = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .delete("/tenants/{id}", request.id)
                .then()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(403, error.code);
        assertEquals("Access denied.", error.message);

    }

    @Test
    public void deleteTenantNotExisting() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
        var error = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .delete("/tenants/{id}", currentMockId)
                .then()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);
        assertEquals(404, error.code);

    }

    @Test
    public void testAssignMultipleProjects() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

        // Create two projects
        var p1 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(buildCreateRequest())
                .post("/projects")
                .then()
                .statusCode(201)
                .extract().as(ProjectResponseDto.class);

        var p2 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(buildCreateRequest())
                .post("/projects")
                .then()
                .statusCode(201)
                .extract().as(ProjectResponseDto.class);

        var req = new TenantProjectRequestDto();
        req.tenantId = request.id;
        req.projectIds = List.of(p1.id, p2.id);

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .put("/tenant-project")
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(200, response.code);
    }

    @Test
    public void testAssignInvalidTenant() {
        var req = new TenantProjectRequestDto();
        req.tenantId = "tenant-not-exist";
        req.projectIds = List.of("proj-not-exist");
        mockSuperAdmin();
        var error = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .put("/tenant-project")
                .then()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(404, error.code);
    }



    @Test
    public void updateTenantStatus() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        mockSuperAdmin();

        var created = createTenant("TENANT-TEST");

        var started = updateManualJob(created.id, EventStatus.IN_PROGRESS, "Creating domain names");
        var startedJob = findJob(started, TenantJobEvent.CREATE_DOMAIN_NAMES);

        assertEquals(EventStatus.IN_PROGRESS.name(), startedJob.status);
        assertEquals("Creating domain names", startedJob.message);
        assertNotNull(startedJob.start);
        assertNull(startedJob.end);

        var completed = updateManualJob(created.id, EventStatus.COMPLETED, "Domain names created");
        var completedJob = findJob(completed, TenantJobEvent.CREATE_DOMAIN_NAMES);

        assertEquals(EventStatus.COMPLETED.name(), completedJob.status);
        assertEquals("Domain names created", completedJob.message);
        assertEquals(startedJob.start, completedJob.start);
        assertNotNull(completedJob.end);
        assertFalse(completedJob.end.isBefore(completedJob.start));
    }



    @Test
    public void updateTenantStatusRerun() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        mockSuperAdmin();

        var created = createTenant("TENANT-TEST");

        updateManualJob(created.id, EventStatus.IN_PROGRESS, "Starting");

        var completed = findJob(
                updateManualJob(created.id, EventStatus.COMPLETED, "Completed"),
                TenantJobEvent.CREATE_DOMAIN_NAMES
        );

        assertNotNull(completed.start);
        assertNotNull(completed.end);

        var initialising = findJob(
                updateManualJob(created.id, EventStatus.INITIALISING, "Rerunning"),
                TenantJobEvent.CREATE_DOMAIN_NAMES
        );

        assertNull(initialising.start);
        assertNull(initialising.end);

        var initialised = findJob(
                updateManualJob(created.id, EventStatus.INITIALISED, "Scheduled"),
                TenantJobEvent.CREATE_DOMAIN_NAMES
        );

        assertNull(initialised.start);
        assertNull(initialised.end);

        var restarted = findJob(
                updateManualJob(created.id, EventStatus.IN_PROGRESS, "Running again"),
                TenantJobEvent.CREATE_DOMAIN_NAMES
        );

        assertNotNull(restarted.start);
        assertNull(restarted.end);

        var failed = findJob(
                updateManualJob(created.id, EventStatus.FAILED, "Failed"),
                TenantJobEvent.CREATE_DOMAIN_NAMES
        );

        assertEquals(EventStatus.FAILED.name(), failed.status);
        assertEquals(restarted.start, failed.start);
        assertNotNull(failed.end);
        assertFalse(failed.end.isBefore(failed.start));
    }



    @Test
    public void testCreateProject() {
        var req = buildCreateRequest();
        mockSuperAdmin();
        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        assertNotNull(created.id);
    }

    @Test
    public void testGetProject() {
        var req = buildCreateRequest();
        mockSuperAdmin();
        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var fetched = given()
                .auth().oauth2(adminToken)
                .get("/projects/" + created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(ProjectResponseDto.class);

        assertEquals(created.id, fetched.id);
        assertEquals(created.name, fetched.name);
    }

    @Test
    public void testUpdateProject() {
        var req = buildCreateRequest();
        mockSuperAdmin();
        var created = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var update = new ProjectUpdateDto();
        update.description = "Project description UPDATE";
        update.dataRetentionPolicy = "Retention policy text";


        var updated = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put("/projects/" + created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(ProjectResponseDto.class);

        assertEquals("Retention policy text", updated.dataRetentionPolicy);
    }

    @Test
    public void testDeleteProject() {
        var req = buildCreateRequest();
        mockSuperAdmin();
        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var response = given()
                .auth().oauth2(adminToken)
                .delete("/projects/" + created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(200, response.code);
        assertEquals("Project has been successfully deleted.", response.message);
    }

    @Test
    public void superAdminFetchAllMembers() {

        mockSuperAdmin();

        var fakeUsers = List.of(
                createFakeUser("1", "user1"),
                createFakeUser("2", "user2"),
                createFakeUser("3", "user3"),
                createFakeUser("4", "user4"),
                createFakeUser("5", "user5")
        );

        var fakePageableResponse = createFakePageableResponse(fakeUsers);

        when(groupManagementService.getAllMembers(
                eq("members"),
                any(),
                eq(0),
                eq(5),
                any()
        )).thenReturn(fakePageableResponse);

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/members?page=1&size=5")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(AdminEndpoint.PageableGroupUserResponse.class);

        assertNotNull(response.getContent());
        assertEquals(5, response.getContent().size());
    }

    private AdminEndpoint.PageableGroupUserResponse createFakePageableResponse(List<GroupUserResponse> users) {
        var response = new AdminEndpoint.PageableGroupUserResponse();
        response.setContent(users);
        response.setSizeOfPage(5);
        response.setNumberOfPage(1);
        response.setTotalElements(5L);
        response.setTotalPages(1);
        response.setLinks(List.of());
        return response;
    }

    private GroupUserResponse createFakeUser(String id, String username) {
        var user = new GroupUserResponse();
        user.id = id;
        user.username = username;
        user.firstName = "First_" + username;
        user.lastName = "Last_" + username;
        user.email = username + "@test.com";
        user.uid = "uid_" + id;
        user.memberships = Map.of(TenantResource.TENANT.resourceName(), List.of());

        return user;
    }

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
       request.performance=Boolean.TRUE;

        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);
    }

    private ProjectRequestDto buildCreateRequest() {

        var dto = new ProjectRequestDto();
        dto.name = "Test Project" + UUID.randomUUID();
        dto.description = "Project description";
        dto.startDate = Timestamp.from(Instant.now());
        dto.endDate = Timestamp.from(Instant.now());
        dto.sustainabilityEndDate = Timestamp.from(Instant.now());
        dto.dataRetentionPolicy = "Retention policy text";

        return dto;
    }

    private TenantStatusFullResponse updateManualJob(String tenantId, EventStatus status, String message) {

        var job = new EventStatusDto();
        job.name = TenantJobEvent.CREATE_DOMAIN_NAMES.key();
        job.status = status.name();
        job.message = message;

        var request = new TenantStatusDto();
        request.jobs = List.of(job);

        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/tenants/{id}/manual/status", tenantId)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantStatusFullResponse.class);
    }

    private EventStatusDto findJob(TenantStatusFullResponse response, TenantJobEvent event) {
        return response.status.jobs.stream()
                .filter(job -> event.key().equalsIgnoreCase(job.name))
                .findFirst()
                .orElseThrow();
    }
}