package org.grnet.status.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.grnet.status.api.endpoints.UserEndpoint;
import org.grnet.status.dtos.user.UpdateUserProfileDto;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(UserEndpoint.class)
public class UserEndpointTest extends KeycloakTest {


    @Inject
    UserService userService;
    @AfterEach
    public void cleanup() {
        userService.deleteAll();
    }

    @Test
    public void registerUser() {
        var response = register(aliceToken);
        assertEquals("alice", response.username);
    }

    @Test
    public void getUserProfile() {
        register(aliceToken);

        var response = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .get("/profile")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(UserProfileDto.class);

        assertEquals("alice", response.username);
    }

    @Test
    public void updateUserProfile() {
        register(aliceToken);

        var updateRequest = new UpdateUserProfileDto();
        updateRequest.name = "FIRST NAME";
        updateRequest.email = "updated@email.com";
        updateRequest.surname = "LAST NAME";

        var response = given()
                .auth().oauth2(aliceToken)
                .body(updateRequest)
                .contentType(ContentType.JSON)
                .put("/profile") // use PUT for updates
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(UserProfileDto.class);

        assertEquals("FIRST NAME", response.name);
        assertEquals("LAST NAME", response.surname);
        assertEquals("updated@email.com", response.email);
    }

    private UserProfileDto register(String token) {
        return given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .post("/register")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(UserProfileDto.class);
    }
}
