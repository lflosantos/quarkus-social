package io.github.lflosantos.quarkussocial.rest.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FollowersByUserResponse {
    private Integer followersCount;
    private List<FollowerResponse> content;
}
