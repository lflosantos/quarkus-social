package io.github.lflosantos.quarkussocial.rest;


import io.github.lflosantos.quarkussocial.domain.model.User;
import io.github.lflosantos.quarkussocial.domain.repository.UserRepository;
import io.github.lflosantos.quarkussocial.rest.dto.CreateUserRequest;
import io.github.lflosantos.quarkussocial.rest.dto.ResponseError;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.google.gson.Gson;

import jakarta.inject.Inject;
import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.Set;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_PLAIN)
public class UserResource {

    private UserRepository repository;
    private final Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator){
        this.repository = repository;
        this.validator = validator;
    }
    Gson gson = new Gson();
    @POST
    @Transactional
    public Response createUser( String userRequest){

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest = gson.fromJson(userRequest, CreateUserRequest.class);

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(createUserRequest);
        if(!violations.isEmpty()){
            ResponseError responseError = ResponseError.createFromValidation(violations);
            return responseError.withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
            //ConstraintViolation<CreateUserRequest> erro = violations.stream().findAny().get();
            //String responseError = erro.getMessage();
//            return Response.status().entity(gson.toJson(responseError)).build();
        }

        User user = new User(createUserRequest.getName(),createUserRequest.getAge());

        //user.persist();
        repository.persist(user);
        return Response.status(Response.Status.CREATED.getStatusCode()).entity(gson.toJson(user)).build();
    }

    @GET
    public Response listAllUsers() {

        //PanacheQuery<User> query = User.findAll();
        PanacheQuery<User> query = repository.findAll();
        ArrayList<User> users = new ArrayList<User>();
        users = (ArrayList<User>) query.list();
        return Response.ok(gson.toJson(query.list())).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, String createUserRequestRequest){

        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest = gson.fromJson(createUserRequestRequest, CreateUserRequest.class);
//        User user = User.findById(id);
        User user = repository.findById(id);
        if(user!=null){
            user.setName(userRequest.getName());
            user.setAge(userRequest.getAge());
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id){
        //User user = User.findById(id);
        User user = repository.findById(id);
        if(user!=null){
            //user.delete();
            repository.delete(user);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }



}
