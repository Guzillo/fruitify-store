package io.github.guzillo.fruitifystorebackend.auth.controller;

import io.github.guzillo.fruitifystorebackend.auth.dto.SignupUserDto;
import io.github.guzillo.fruitifystorebackend.auth.service.SignupService;
import io.github.guzillo.fruitifystorebackend.auth.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;
    private final SignupService signupService;

    @PostMapping("/signin")
    public String token(Authentication authentication) {
        return tokenService.generateToken(authentication);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody SignupUserDto signupUserDto) {
        signupService.signup(signupUserDto);
    }

}
