package io.github.guzillo.fruitifystorebackend.auth.service;

import io.github.guzillo.fruitifystorebackend.auth.dto.SignupUserDto;
import io.github.guzillo.fruitifystorebackend.auth.exception.EmailAlreadyRegisteredException;
import io.github.guzillo.fruitifystorebackend.security.repository.RoleRepository;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public void signup(SignupUserDto signupUserDto) {
        Optional<User> foundUser = userRepository.findByEmail(signupUserDto.email());
        if (foundUser.isPresent()) {
            throw new EmailAlreadyRegisteredException(signupUserDto.email());
        }

        User user = new User(
            signupUserDto.email().toLowerCase(),
            passwordEncoder.encode(signupUserDto.password()),
            Set.of(roleRepository.findByName("ROLE_USER"))
        );
        user.setFirstName(signupUserDto.firstName());
        user.setLastName(signupUserDto.lastName());

        userRepository.save(user);
    }

}
