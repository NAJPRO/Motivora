package com.audin.motivora.dto.request;

import com.audin.motivora.dto.Annotation.UniqueField;
import com.audin.motivora.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTORequest {

    private static final String NAME_PATTERN = "^\\p{L}+([ '-]\\p{L}+)*$";

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = NAME_PATTERN, message = "First name contains invalid characters")
    private String first_name;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = NAME_PATTERN, message = "Last name contains invalid characters")
    private String last_name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @UniqueField(entity = User.class, fieldName = "email", message = "Email already used")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Password must contain at least one uppercase letter, one lowercase letter and one number")
    private String password;
}
