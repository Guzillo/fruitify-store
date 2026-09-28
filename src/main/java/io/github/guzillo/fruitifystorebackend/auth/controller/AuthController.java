package io.github.guzillo.fruitifystorebackend.auth.controller;

import io.github.guzillo.fruitifystorebackend.auth.dto.AuthenticationResponse;
import io.github.guzillo.fruitifystorebackend.auth.dto.RefreshTokenRequest;
import io.github.guzillo.fruitifystorebackend.auth.dto.SignupUserDto;
import io.github.guzillo.fruitifystorebackend.auth.service.RefreshTokenService;
import io.github.guzillo.fruitifystorebackend.auth.service.SignupService;
import io.github.guzillo.fruitifystorebackend.auth.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final SignupService signupService;

    @PostMapping("/signin")
    public AuthenticationResponse authenticate(Authentication authentication) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
        return new AuthenticationResponse(
            tokenService.generateToken(userDetails),
            refreshTokenService.generateRefreshToken(userDetails.getUsername())
        );
    }

    @PostMapping("/refresh")
    public AuthenticationResponse refresh(
        @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String username = refreshTokenService.validateRefreshTokenAndGetUsername(
            refreshTokenRequest.refreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new AuthenticationResponse(
            tokenService.generateToken(userDetails),
            refreshTokenRequest.refreshToken()
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.revokeRefreshToken(refreshTokenRequest.refreshToken());
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody SignupUserDto signupUserDto) {
        signupService.signup(signupUserDto);
    }

}
