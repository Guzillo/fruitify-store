package io.github.guzillo.fruitifystorebackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for {@link io.github.guzillo.fruitifystorebackend.user.model.User}
 */
@Value
public class SignupUserDto implements Serializable {

    @NotNull
    @Email
    String email;
    @Length(min = 8)
    String password;
}