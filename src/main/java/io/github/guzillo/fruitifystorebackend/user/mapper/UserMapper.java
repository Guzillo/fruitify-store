package io.github.guzillo.fruitifystorebackend.user.mapper;

import io.github.guzillo.fruitifystorebackend.user.dto.UserResponseDTO;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
            user.getEmail(),
            user.getFirstName(),
            user.getLastName()
        );
    }
}
