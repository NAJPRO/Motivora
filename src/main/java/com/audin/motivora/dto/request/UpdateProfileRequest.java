package com.audin.motivora.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    private static final String PSEUDO_PATTERN = "^\\p{L}+([ '-]\\p{L}+)*$";

    @Size(min = 2, max = 50, message = "Pseudo must be between 2 and 50 characters")
    @Pattern(regexp = PSEUDO_PATTERN, message = "Pseudo contains invalid characters")
    private String pseudo;
}
