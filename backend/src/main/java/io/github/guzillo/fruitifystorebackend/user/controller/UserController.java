package io.github.guzillo.fruitifystorebackend.user.controller;

import io.github.guzillo.fruitifystorebackend.user.dto.UpdateUserReqDTO;
import io.github.guzillo.fruitifystorebackend.user.dto.UpdatePasswordReqDTO;
import io.github.guzillo.fruitifystorebackend.user.service.UserService;
import io.github.guzillo.fruitifystorebackend.user.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Profile and password of the authenticated user")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public UserResponseDTO me(Authentication authentication) {
        String email = authentication.getName();

        return userService.findUserByEmailAndReturnDto(email);
    }

    @Operation(summary = "Update my profile")
    @PutMapping("/me")
    public UserResponseDTO updateMe(@RequestBody UpdateUserReqDTO updateUserReqDTO,
        Authentication authentication) {
        return userService.updateUser(authentication, updateUserReqDTO);
    }

    @Operation(summary = "Change my password")
    @PutMapping("/me/password")
    public void updatePassword(@RequestBody UpdatePasswordReqDTO updatePasswordReqDTO,
        Authentication authentication) {
        userService.updatePassword(authentication, updatePasswordReqDTO);
    }
}
