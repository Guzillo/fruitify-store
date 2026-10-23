package io.github.guzillo.fruitifystorebackend.auth.dto;

import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for {@link User}
 */
public record SignupUserDto(
    @Schema(description = "Unique email address", example = "jan@example.com")
    @NotBlank @Email String email,
    @Schema(description = "Password, at least 8 characters", example = "haslo12345")
    @NotBlank @Length(min = 8) String password,
    @Schema(description = "First name", example = "Jan")
    String firstName,
    @Schema(description = "Last name", example = "Kowalski")
    String lastName) implements Serializable {

    public SignupUserDto(String email, String password) {
        this(email, password, null, null);
    }

}