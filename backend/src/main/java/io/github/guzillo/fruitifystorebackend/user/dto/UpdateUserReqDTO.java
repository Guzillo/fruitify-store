package io.github.guzillo.fruitifystorebackend.user.dto;

import jakarta.validation.constraints.Email;
import java.io.Serializable;

/**
 * DTO for {@link io.github.guzillo.fruitifystorebackend.user.model.User}
 */
public record UpdateUserReqDTO(@Email String email, String lastName, String firstName) implements
    Serializable {

}