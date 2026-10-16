package io.github.guzillo.fruitifystorebackend.user.dto;

import java.io.Serializable;

/**
 * DTO for {@link io.github.guzillo.fruitifystorebackend.user.model.User}
 */
public record UserDto(String email, String firstName, String lastName) implements Serializable {

}