package io.github.lflosantos.quarkussocial.rest;

import com.google.gson.Gson;
import io.github.lflosantos.quarkussocial.domain.model.Follower;
import io.github.lflosantos.quarkussocial.domain.model.Post;
import io.github.lflosantos.quarkussocial.domain.model.User;
import io.github.lflosantos.quarkussocial.domain.repository.FollowerRepository;
import io.github.lflosantos.quarkussocial.domain.repository.UserRepository;
import io.github.lflosantos.quarkussocial.rest.dto.FollowRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;

    Long userIdCriado, userFollowerId, userNotFollowerId;
    Gson gson;

    @BeforeEach
    @Transactional
    void setup(){
        var user = new User();
        user.setName("Fulano");
        user.setAge(99);
        userRepository.persist(user);
        userIdCriado = user.getId();

        var userNotFollower = new User();
        userNotFollower.setName("Beltrano");
        userNotFollower.setAge(77);
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        var userFollow = new User();
        userFollow.setName("Ciclano");
        userFollow.setAge(88);
        userRepository.persist(userFollow);
        userFollowerId = userFollow.getId();

        var follow = new Follower();
        follow.setUser(user);
        follow.setFollower(userFollow);
        followerRepository.persist(follow);

    }

    @Test
    @DisplayName("Listando seguidores com sucesso")
    void listFollowers() {

//        given()
//                .contentType(ContentType.JSON)
//                .pathParam("userId",userIdCriado)
//        .when()
//                .get()
//        .then()
//                .statusCode(Response.Status.OK.getStatusCode())
//                .body("followersCount",Matchers.is(1))
//                .body("content.size()",Matchers.is(1));

        var response = given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userIdCriado)
        .when()
                .get()
        .then()
                .extract().response();

        var followerCount = response.jsonPath().get("followersCount");
        var followerContent = response.jsonPath().getList("content");

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(followerCount,1);
        assertEquals(followerCount, followerContent.size());

    }

    @Test
    @DisplayName("Listando seguidores de usuário não cadastrado")
    void  listFollowersUserNotFound() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",999)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(Matchers.is("Usuário não encontrado"));
    }

    @Test
    @DisplayName("Seguir usuario com sucesso")
    void followUser() {

        FollowRequest followRequest = new FollowRequest();
        followRequest.setUserId(userNotFollowerId);

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
                .body(followRequest)
        .when()
                .put()
        .then().statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.is("Seguindo com sucesso"));
    }

    @Test
    @DisplayName("Usuario seguir o proprio usuario")
    void followUserbyUser() {

        FollowRequest followRequest = new FollowRequest();
        followRequest.setUserId(userIdCriado);

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
                .body(followRequest)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode())
                .body(Matchers.is("O usuário não pode se seguir"));


    }

    @Test
    @DisplayName("Usuario não encontrado")
    void followUserNotFound() {

        FollowRequest followRequest = new FollowRequest();
        followRequest.setUserId(userNotFollowerId);

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",999)
                .body(followRequest)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(Matchers.is("Usuário não encontrado"));

    }

    @Test
    @DisplayName("Seguidor não encontrado")
    void followUserFollowerNotFound() {

        FollowRequest followRequest = new FollowRequest();
        followRequest.setUserId(999L);

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
                .body(followRequest)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(Matchers.is("Seguidor não encontrado"));

    }

    @Test
    @DisplayName("Deixar de seguir usuário com sucesso")
    void unfollow() {

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
                .queryParam("followerId",userFollowerId)
        .when()
                .delete()
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.is("Deixou de seguir com sucesso"));

    }

    @Test
    @DisplayName("Deixar de seguir usuário inexistente")
    void unfollowUserNotFound() {

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",999)
                .queryParam("followerId",userFollowerId)
        .when()
                .delete()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(Matchers.is("Usuário não encontrado"));

    }

    @Test
    @DisplayName("Seguidor inexistente deixando de seguir")
    void unfollowFollowerNotFound() {

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
                .queryParam("followerId",999)
        .when()
                .delete()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(Matchers.is("Seguidor não encontrado"));
    }

}