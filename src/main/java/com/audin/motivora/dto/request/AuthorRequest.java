package com.audin.motivora.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorRequest {
    @NotBlank(message = "Name required")
    @Size(max = 100, message = "Name max length is 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Bio max length is 150 characters")
    private String bio;

    private String avatarUrl;
}
