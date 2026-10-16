package io.github.guzillo.fruitifystorebackend.user;

import io.github.guzillo.fruitifystorebackend.user.dto.UserDto;
import io.github.guzillo.fruitifystorebackend.user.exception.AuthenticatedUserNotFoundException;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto findUserByEmailAndReturnDto(String email) {
        return userRepository.findByEmail(email)
            .map(user -> new UserDto(user.getEmail(), user.getFirstName(), user.getLastName()))
            .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
    }
}
