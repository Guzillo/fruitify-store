package io.github.guzillo.fruitifystorebackend.security.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guzillo.fruitifystorebackend.user.model.User;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SecurityUserTest {

    @Test
    void shouldExposeUserCredentialsAndRolePrivileges() {
        Privilege read = new Privilege("READ_PRIVILEGE");
        Role role = new Role("ROLE_USER");
        role.setPrivileges(Set.of(read));
        User user = new User("john@example.com", "encoded", Set.of(role));

        SecurityUser securityUser = new SecurityUser(user);

        assertThat(securityUser.getUsername()).isEqualTo("john@example.com");
        assertThat(securityUser.getPassword()).isEqualTo("encoded");
        assertThat(securityUser.getAuthorities())
            .extracting("authority")
            .containsExactlyInAnyOrder("ROLE_USER", "READ_PRIVILEGE");
    }

    @Test
    void shouldHaveNoAuthoritiesWhenUserHasNoRoles() {
        User user = new User("john@example.com", "encoded", Set.of());

        assertThat(new SecurityUser(user).getAuthorities()).isEmpty();
    }
}
