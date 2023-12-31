package io.github.lflosantos.quarkussocial.rest.dto;

import io.github.lflosantos.quarkussocial.domain.model.Post;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostResponse {
    private String text;
    private LocalDateTime dateTime;

    public static PostResponse fromEntity(Post post){
        return new PostResponse(post.getText(), post.getDateTime());
    }
}
