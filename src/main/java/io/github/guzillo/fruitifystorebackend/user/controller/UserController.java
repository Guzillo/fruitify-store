package io.github.guzillo.fruitifystorebackend.user.controller;

import io.github.guzillo.fruitifystorebackend.user.UserService;
import io.github.guzillo.fruitifystorebackend.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserDto me(Authentication authentication) {
        String email = authentication.getName();

        return userService.findUserByEmailAndReturnDto(email);
    }

}
