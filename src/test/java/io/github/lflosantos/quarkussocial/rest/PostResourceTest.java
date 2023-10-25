package io.github.lflosantos.quarkussocial.rest;

import io.github.lflosantos.quarkussocial.domain.model.Follower;
import io.github.lflosantos.quarkussocial.domain.model.Post;
import io.github.lflosantos.quarkussocial.domain.model.User;
import io.github.lflosantos.quarkussocial.domain.repository.FollowerRepository;
import io.github.lflosantos.quarkussocial.domain.repository.PostRepository;
import io.github.lflosantos.quarkussocial.domain.repository.UserRepository;
import io.github.lflosantos.quarkussocial.rest.dto.CreatePostRequest;
import io.github.lflosantos.quarkussocial.rest.dto.CreateUserRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    PostRepository postRepository;

    @Inject
    FollowerRepository followerRepository;

    Long userIdCriado, userFollowerId, userNotFollowerId;

    @BeforeEach
    @Transactional
    public void setup(){
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

        var post = new Post("Testando POST!!!", LocalDateTime.now(),user);
        postRepository.persist(post);
    }

    @Test
    @DisplayName("Deve criar post para o usuário")
    void savePost() {

        var postRequest = new CreatePostRequest();
        postRequest.setText("Qualquer coisa");


        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParam("userId",userIdCriado)
        .when()
                .post()
        .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Não deve criar post para usuário inexistente")
    void savePostUserNotFound() {

        var postRequest = new CreatePostRequest();
        postRequest.setText("Qualquer coisa");
        var userId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParam("userId",userId)
        .when()
                .post()
        .then()
                .statusCode(RestResponse.StatusCode.NOT_FOUND);
    }


    @Test
    void listPosts() {

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
                .header("followerId",userFollowerId)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("size()", Matchers.is(1));

    }

    @Test
    @DisplayName("Deve retornar 404 quando o usuário não existe")
    void listPostsUerNotFound() {

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",999)
                .header("followerId",888)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(Matchers.is("Usuário inexistente."));

    }

    @Test
    @DisplayName("Deve retornar 400 quando o seguidor não foi enviado no HEADER")
    void listPostsNotFollowerHeader() {

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body(Matchers.is("Você esqueceu o header followerId"));


    }

    @Test
    @DisplayName("Deve retornar 400 quando o seguidor não existe")
    void listPostsFollowerNotFound() {

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
                .header("followerId",888)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body(Matchers.is("Seguidor inexistente"));

    }

    @Test
    @DisplayName("Deve retornar 403 quando o seguidor não segue")
    @Transactional
    void listPostsNotFollower() {

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId",userIdCriado)
                .header("followerId",userNotFollowerId)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .body(Matchers.is("Você não segue o usuário logo não pode ver os seus posts"));
    }

    @Test
    void deletePost() {
    }
}