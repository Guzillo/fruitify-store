package io.github.guzillo.fruitifystorebackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public record UpdatePasswordReqDTO(
    @NotBlank String currentPassword,
    @NotBlank @Size(min = 8) String newPassword
) implements Serializable {

}
