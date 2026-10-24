package io.github.guzillo.fruitifystorebackend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.auth.dto.SignupUserDto;
import io.github.guzillo.fruitifystorebackend.auth.exception.EmailAlreadyRegisteredException;
import io.github.guzillo.fruitifystorebackend.cart.model.Cart;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartRepository;
import io.github.guzillo.fruitifystorebackend.security.model.Role;
import io.github.guzillo.fruitifystorebackend.security.repository.RoleRepository;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class SignupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private SignupService signupService;

    @Test
    void shouldSignupUserWhenEmailIsNotRegistered() {
        SignupUserDto signupUserDto = new SignupUserDto(
            "JOHN@EXAMPLE.COM",
            "plain-password",
            "John",
            "Doe"
        );
        Role userRole = new Role("ROLE_USER");

        when(userRepository.findByEmail(signupUserDto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(signupUserDto.password())).thenReturn("encoded-password");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        signupService.signup(signupUserDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRoles()).containsExactly(userRole);
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
        verify(passwordEncoder).encode("plain-password");
        verify(roleRepository).findByName("ROLE_USER");

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        assertThat(cartCaptor.getValue().getUser()).isSameAs(savedUser);
    }

    @Test
    void shouldRejectSignupWhenEmailIsAlreadyRegistered() {
        SignupUserDto signupUserDto = new SignupUserDto(
            "john@example.com",
            "plain-password"
        );
        User existingUser = new User();

        when(userRepository.findByEmail(signupUserDto.email()))
            .thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> signupService.signup(signupUserDto))
            .isInstanceOf(EmailAlreadyRegisteredException.class)
            .hasMessage("Email is already registered: john@example.com");

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
        verifyNoInteractions(passwordEncoder, roleRepository, cartRepository);
    }
}
