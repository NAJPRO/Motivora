package com.audin.motivora.dto.request;

import com.audin.motivora.dto.Annotation.ExistField;
import com.audin.motivora.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordConfirmRequest {

    @NotBlank(message = "Email required")
    @Email(message = "Email is not valid")
    @ExistField(entity = User.class, fieldName = "email", message = "Account don't exist")
    private String email;

    @NotBlank(message = "OTP code is required")
    private String otp;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Password must contain at least one uppercase letter, one lowercase letter and one number")
    private String newPassword;
}
