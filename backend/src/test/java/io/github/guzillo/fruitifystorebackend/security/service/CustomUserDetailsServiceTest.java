package io.github.guzillo.fruitifystorebackend.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.security.model.SecurityUser;
import io.github.guzillo.fruitifystorebackend.security.model.Role;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldLoadUserWithSecurityAuthorities() {
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        user.setRoles(java.util.Set.of(new Role("ROLE_USER")));
        when(userRepository.findWithRolesAndPrivilegesByEmail("john@example.com"))
            .thenReturn(Optional.of(user));

        SecurityUser result = (SecurityUser) userDetailsService
            .loadUserByUsername("john@example.com");

        assertThat(result.getUsername()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getAuthorities())
            .extracting("authority")
            .contains("ROLE_USER");
        verify(userRepository).findWithRolesAndPrivilegesByEmail("john@example.com");
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findWithRolesAndPrivilegesByEmail("missing@example.com"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("missing@example.com");
    }
}
