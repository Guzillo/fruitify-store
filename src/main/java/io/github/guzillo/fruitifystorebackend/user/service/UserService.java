package io.github.guzillo.fruitifystorebackend.user.service;

import io.github.guzillo.fruitifystorebackend.user.dto.UpdateUserReqDTO;
import io.github.guzillo.fruitifystorebackend.user.dto.UserResponseDTO;
import io.github.guzillo.fruitifystorebackend.user.exception.AuthenticatedUserNotFoundException;
import io.github.guzillo.fruitifystorebackend.user.mapper.UserMapper;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDTO findUserByEmailAndReturnDto(String email) {
        User user = findUserByEmail(email);
        return userMapper.toUserResponseDTO(user);
    }

    public UserResponseDTO updateUser(Authentication authentication,
        UpdateUserReqDTO updateUserReqDTO) {
        User user = findUserByEmail(authentication.getName());

        if (updateUserReqDTO.firstName() != null) {
            user.setFirstName(updateUserReqDTO.firstName());
        }

        if (updateUserReqDTO.lastName() != null) {
            user.setLastName(updateUserReqDTO.lastName());
        }

        if (updateUserReqDTO.email() != null) {
            user.setEmail(updateUserReqDTO.email());
        }

        User updated = userRepository.save(user);
        return userMapper.toUserResponseDTO(updated);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
    }



}
