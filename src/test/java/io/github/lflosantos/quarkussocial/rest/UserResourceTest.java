package io.github.lflosantos.quarkussocial.rest;

import io.github.lflosantos.quarkussocial.rest.dto.CreateUserRequest;
import io.github.lflosantos.quarkussocial.rest.dto.ResponseError;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    @TestHTTPResource("/users")
    URL apiURL;

    @Test
    @DisplayName("Deve criar um usuário com sucesso.")
    @Order(1)
    public void createUser() {
        var user = new CreateUserRequest();
        user .setName("Fulano");
        user.setAge(99);

        var response =
            given()
                .contentType(ContentType.JSON)
                .body(user)
            .when()
                    .post(apiURL)
            .then()
                    .extract().response();

        assertEquals(201, response.statusCode());
        assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    @Order(2)
    @DisplayName("Deve retornar erro quando o JSON não é valido.")
    public void createUserFieldError() {
        var user = new CreateUserRequest();
        user .setName(null);
        user.setAge(null);

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(user)
                .when()
                        .post(apiURL)
                .then()
                        .extract().response();

        assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
        //assertEquals("Validation Error",response.jsonPath().getString("message"));
    }

    @Test
    @Order(3)
    @DisplayName("Deve listar todos os usuarios")
    void listAllUsers() {

        given()
                .contentType(ContentType.JSON)
        .when()
                .get(apiURL)
        .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }
}