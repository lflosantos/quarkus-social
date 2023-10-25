package io.github.lflosantos.quarkussocial.rest;

import com.google.gson.Gson;
import io.github.lflosantos.quarkussocial.domain.model.Follower;
import io.github.lflosantos.quarkussocial.domain.model.User;
import io.github.lflosantos.quarkussocial.domain.repository.FollowerRepository;
import io.github.lflosantos.quarkussocial.domain.repository.UserRepository;
import io.github.lflosantos.quarkussocial.rest.dto.FollowRequest;
import io.github.lflosantos.quarkussocial.rest.dto.FollowerResponse;
import io.github.lflosantos.quarkussocial.rest.dto.FollowersByUserResponse;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository repository;
    private UserRepository userRepository;
    Gson gson = new Gson();

    public FollowerResource(FollowerRepository repository, UserRepository userRepository){
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId){

        User user = userRepository.findById(userId);
        if (user==null){
            return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
        }

        var followers = repository.findByUser(userId);
        FollowersByUserResponse followersByUserResponse = new FollowersByUserResponse();
        followersByUserResponse.setFollowersCount(followers.size());

        var follewersList = followers.stream().map( FollowerResponse::new ).collect(Collectors.toList());
        followersByUserResponse.setContent(follewersList);
        return Response.ok(gson.toJson(followersByUserResponse)).build();
    }

    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId, String request){

        FollowRequest followRequest = gson.fromJson(request, FollowRequest.class);
        if(userId.equals(followRequest.getUserId())){
            return Response.status(Response.Status.CONFLICT).entity("O usuário não pode se seguir").build();
        }
        User user = userRepository.findById(userId);
        if (user==null){
            return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
        }

        User user2 = userRepository.findById(followRequest.getUserId());
        if (user2==null){
            return Response.status(Response.Status.NOT_FOUND).entity("Seguidor não encontrado").build();
        }
        if(!repository.follows(user,user2)){
            Follower follower = new Follower();
            follower.setUser(user);
            follower.setFollower(user2);
            repository.persist(follower);
        }

        return Response.status(Response.Status.OK)
                        .entity("Seguindo com sucesso").build();
    }

    @DELETE
    @Transactional
    public Response unfollow(@PathParam("userId") Long userId,@QueryParam("followerId") Long followerId ){

        User user = userRepository.findById(userId);
        if (user==null){
            return Response.status(Response.Status.NOT_FOUND).entity("Usuário não encontrado").build();
        }

        User follower = userRepository.findById(followerId);
        if (follower==null){
            return Response.status(Response.Status.NOT_FOUND).entity("Seguidor não encontrado").build();
        }

        Follower followerByUser = repository.findByUserFollower(user, follower);
        if(followerByUser!=null){
            repository.delete(followerByUser);
        }

        return Response.status(Response.Status.OK)
                .entity("Deixou de seguir com sucesso").build();
    }

}
