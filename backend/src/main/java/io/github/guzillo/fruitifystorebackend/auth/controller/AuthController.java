package io.github.guzillo.fruitifystorebackend.auth.controller;

import io.github.guzillo.fruitifystorebackend.auth.dto.AuthenticationResponse;
import io.github.guzillo.fruitifystorebackend.auth.dto.RefreshTokenRequest;
import io.github.guzillo.fruitifystorebackend.auth.dto.SignupUserDto;
import io.github.guzillo.fruitifystorebackend.auth.service.RefreshTokenService;
import io.github.guzillo.fruitifystorebackend.auth.service.SignupService;
import io.github.guzillo.fruitifystorebackend.auth.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Authentication", description = "Registration, sign-in, token refresh and logout")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final SignupService signupService;

    @Operation(summary = "Sign in",
        description = "Authenticates with HTTP Basic credentials and returns a JWT access token "
            + "and a refresh token.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Authenticated")
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    @PostMapping("/signin")
    public AuthenticationResponse authenticate(Authentication authentication) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
        return new AuthenticationResponse(
            tokenService.generateToken(userDetails),
            refreshTokenService.generateRefreshToken(userDetails.getUsername())
        );
    }

    @Operation(summary = "Refresh access token",
        description = "Exchanges a valid refresh token for a new access token.")
    @ApiResponse(responseCode = "200", description = "New access token issued")
    @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
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

    @Operation(summary = "Logout", description = "Revokes the given refresh token.")
    @ApiResponse(responseCode = "204", description = "Refresh token revoked")
    @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.revokeRefreshToken(refreshTokenRequest.refreshToken());
    }

    @Operation(summary = "Register a new user",
        description = "Creates a user account with the USER role.")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "409", description = "Email is already registered")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody SignupUserDto signupUserDto) {
        signupService.signup(signupUserDto);
    }

}
