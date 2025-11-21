package org.grnet.status.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.grnet.status.api.endpoints.AdminEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.user.UserProfileDto;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(AdminEndpoint.class)
public class AdminEndpointTest extends KeycloakTest {

    // -------------------------------------------------------------
    // ENTITLEMENTS TESTING
    // -------------------------------------------------------------
    @Test
    public void superAdminCanFetchAllPages() {
        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/pages?page=1&size=5")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(PageResource.class);
    }

    @Test
    public void normalUserCannotFetchAllPages() {
        var error = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/pages?page=1&size=5")
                .then()
                .assertThat()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Access denied — super admin privileges required.", error.message);
    }

    @Test
    public void superAdminCanFetchAllUsers() {
        var users = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/users")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(UserProfileDto[].class);
    }

    @Test
    public void normalUserCannotFetchAllUsers() {
        var error = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/users")
                .then()
                .assertThat()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Access denied — super admin privileges required.", error.message);
    }
}
