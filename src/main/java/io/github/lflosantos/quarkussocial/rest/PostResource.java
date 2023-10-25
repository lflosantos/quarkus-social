package io.github.lflosantos.quarkussocial.rest;

import com.google.gson.Gson;
import io.github.lflosantos.quarkussocial.domain.model.Post;
import io.github.lflosantos.quarkussocial.domain.model.User;
import io.github.lflosantos.quarkussocial.domain.repository.FollowerRepository;
import io.github.lflosantos.quarkussocial.domain.repository.PostRepository;
import io.github.lflosantos.quarkussocial.domain.repository.UserRepository;
import io.github.lflosantos.quarkussocial.rest.dto.CreatePostRequest;
import io.github.lflosantos.quarkussocial.rest.dto.PostResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private Gson gson = new Gson();
    private UserRepository userRepository;
    private final PostRepository postRepository;
    private final FollowerRepository followerRepository;

    @Inject
    public PostResource(UserRepository userRepository, PostRepository postRepository, FollowerRepository followerRepository){
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long userId, String request){
        User user = userRepository.findById(userId);
        if(user==null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        CreatePostRequest createPostRequest = gson.fromJson(request,CreatePostRequest.class);
        Post post = new Post();
        post.setUser(user);
        post.setText(createPostRequest.getText());

        postRepository.persist(post);
        return Response.ok().build();
    }

    @GET
    public Response listPosts(@PathParam("userId") Long userId, @HeaderParam("followerId") Long followerId){
        User user = userRepository.findById(userId);
        if (user==null){
            return Response.status(Response.Status.NOT_FOUND).entity("Usuário inexistente.").build();
        }
        if (followerId==null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Você esqueceu o header followerId").build();
        }

        User follower = userRepository.findById(followerId);
        if (follower==null){
            return Response.status(Response.Status.NOT_FOUND).entity("Seguidor inexistente").build();
        }

        if (!followerRepository.follows(user, follower)){
            return Response.status(Response.Status.FORBIDDEN).entity("Você não segue o usuário logo não pode ver os seus posts").build();
        }
        
        PanacheQuery<Post> postPanacheQuery = postRepository.find("user",Sort.by("dateTime", Sort.Direction.Descending), user);
        var list = postPanacheQuery.list();
        List<PostResponse> postResponses = list.stream()
                                            //.map(post -> PostResponse.fromEntity(post)) - Da no mesmo que a linha de baixo
                                            .map(PostResponse::fromEntity)
                                            .collect(Collectors.toList());
        return Response.ok(gson.toJson(postResponses)).build();
    }

    @DELETE
    @Path("/{postId}")
    @Transactional
    public Response deletePost(@PathParam("userId") Long userId, @PathParam("postId") Long postId){
        User user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Post post = postRepository.findById(postId);
        if(post == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        postRepository.delete(post);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
