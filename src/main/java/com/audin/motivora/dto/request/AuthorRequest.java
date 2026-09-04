package com.audin.motivora.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    //@Pattern(regexp = "^(https?://.*\\.(png|jpg|jpeg|webp|svg))$", message = "Avatar must be a valid image URL (png, jpg, jpeg, webp, svg)")
    private String avatarUrl;
}
