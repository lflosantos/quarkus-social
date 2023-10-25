package io.github.lflosantos.quarkussocial.domain.repository;

import io.github.lflosantos.quarkussocial.domain.model.Follower;
import io.github.lflosantos.quarkussocial.domain.model.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class FollowerRepository implements PanacheRepository<Follower> {

    public boolean follows(User user, User follower){
        /*Map<String,Object> params = new HashMap<>();
        params.put("user",user);
        params.put("follower",follower);*/
        var params = Parameters.with("user", user).and("follower", follower).map();

        PanacheQuery<Follower> query = find("user= :user and follower= :follower", params);
        Optional<Follower> resultOptional = query.firstResultOptional();
        return resultOptional.isPresent();
    }

    public List<Follower> findByUser(Long userId){
        var param = Parameters.with("userId", userId);
        PanacheQuery<Follower> query = find("user.id = :userId", param);
        return query.list();
    }

    public Follower findByUserFollower(User user, User follower) {
        var param = Parameters.with("user",user.getId()).and("follower",follower.getId());
        PanacheQuery<Follower> query = find("user.id = :user and follower.id = :follower", param);
        return query.firstResult();
    }
}
