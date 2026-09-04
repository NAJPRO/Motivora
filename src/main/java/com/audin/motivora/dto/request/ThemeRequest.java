package com.audin.motivora.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThemeRequest {
    @NotBlank(message = "Theme name is required")
    @Size(min = 3, max = 50, message = "Theme name must be between 3 and 50 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Color is required")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex color (e.g. #FF5733)")
    private String color;

    //@Pattern(regexp = "^(https?://.*\\.(png|jpg|jpeg|webp|svg))$", message = "Image must be a valid URL to an image (png, jpg, jpeg, webp, svg)")
    private String imageUrl;

}
