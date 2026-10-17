package io.github.guzillo.fruitifystorebackend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.user.dto.UpdateUserReqDTO;
import io.github.guzillo.fruitifystorebackend.user.dto.UserResponseDTO;
import io.github.guzillo.fruitifystorebackend.user.mapper.UserMapper;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReceiveUpdatedUser() {
        User user = new User();
        user.setEmail("old@example.com");
        user.setFirstName("Old");
        user.setLastName("Name");

        UpdateUserReqDTO request = new UpdateUserReqDTO(
            "new@example.com",
            "New Name",
            "New"
        );
        UserResponseDTO expectedResponse = new UserResponseDTO(
            "new@example.com",
            "New",
            "New Name"
        );

        when(authentication.getName()).thenReturn("old@example.com");
        when(userRepository.findByEmail("old@example.com")).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(expectedResponse);

        UserResponseDTO result = userService.updateUser(authentication, request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("New Name");
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDTO(user);
    }
}
