package io.github.guzillo.fruitifystorebackend.auth.dto;

import io.github.guzillo.fruitifystorebackend.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for {@link User}
 */
public record SignupUserDto(@NotBlank @Email String email,
                            @NotBlank @Length(min = 8) String password,
                            String firstName,
                            String lastName) implements Serializable {

    public SignupUserDto(String email, String password) {
        this(email, password, null, null);
    }

}