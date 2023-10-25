package io.github.lflosantos.quarkussocial.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CreateUserRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;
    @NotNull(message = "Idade é obrigatória")
    private Integer age;

}
