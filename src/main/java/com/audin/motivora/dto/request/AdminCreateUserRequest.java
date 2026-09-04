package com.audin.motivora.dto.request;

import com.audin.motivora.dto.Annotation.UniqueField;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateUserRequest {

    @NotBlank(message = "Pseudo is required")
    @Size(min = 2, max = 50, message = "Pseudo must be between 2 and 50 characters")
    private String pseudo;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @UniqueField(entity = User.class, fieldName = "email", message = "Email already used")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Password must contain at least one uppercase letter, one lowercase letter and one number")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;
}
