package io.github.guzillo.fruitifystorebackend.auth.service;

import io.github.guzillo.fruitifystorebackend.auth.dto.SignupUserDto;
import io.github.guzillo.fruitifystorebackend.auth.exception.EmailAlreadyRegisteredException;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupUserDto signupUserDto) {
        Optional<User> foundUser = userRepository.findByEmail(signupUserDto.getEmail());
        if (foundUser.isPresent()) {
            throw new EmailAlreadyRegisteredException(signupUserDto.getEmail());
        }

        User user = new User(
            signupUserDto.getEmail().toLowerCase(),
            passwordEncoder.encode(signupUserDto.getPassword()),
            "ROLE_USER"
        );

        userRepository.save(user);
    }

}
