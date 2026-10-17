package io.github.guzillo.fruitifystorebackend.user.controller;

import io.github.guzillo.fruitifystorebackend.user.dto.UpdateUserReqDTO;
import io.github.guzillo.fruitifystorebackend.user.dto.UpdatePasswordReqDTO;
import io.github.guzillo.fruitifystorebackend.user.service.UserService;
import io.github.guzillo.fruitifystorebackend.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDTO me(Authentication authentication) {
        String email = authentication.getName();

        return userService.findUserByEmailAndReturnDto(email);
    }

    @PutMapping("/me")
    public UserResponseDTO updateMe(@RequestBody UpdateUserReqDTO updateUserReqDTO,
        Authentication authentication) {
        return userService.updateUser(authentication, updateUserReqDTO);
    }

    @PutMapping("/me/password")
    public void updatePassword(@RequestBody UpdatePasswordReqDTO updatePasswordReqDTO,
        Authentication authentication) {
        userService.updatePassword(authentication, updatePasswordReqDTO);
    }
}
