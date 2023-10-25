package io.github.lflosantos.quarkussocial.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FollowRequest {
    @NotBlank(message = "ID é obrigatório")
    private Long userId;
}
