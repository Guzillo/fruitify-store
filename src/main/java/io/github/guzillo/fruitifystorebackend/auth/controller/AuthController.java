package io.github.guzillo.fruitifystorebackend.auth.controller;

import io.github.guzillo.fruitifystorebackend.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;

    @PostMapping("/signin")
    public String token(Authentication authentication) {
        return tokenService.generateToken(authentication);
    }

}
