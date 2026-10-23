package io.github.guzillo.fruitifystorebackend.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guzillo.fruitifystorebackend.security.model.Privilege;
import io.github.guzillo.fruitifystorebackend.security.model.Role;
import io.github.guzillo.fruitifystorebackend.security.repository.PrivilegeRepository;
import io.github.guzillo.fruitifystorebackend.security.repository.RoleRepository;
import io.github.guzillo.fruitifystorebackend.support.AbstractIntegrationTest;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Test
    void shouldLoadUserRolesAndPrivilegesWithEntityGraph() {
        Privilege privilege = privilegeRepository.save(new Privilege("READ_PRODUCTS"));
        Role role = new Role("ROLE_CATALOG");
        role.setPrivileges(Set.of(privilege));
        role = roleRepository.saveAndFlush(role);
        userRepository.saveAndFlush(new User(
            "catalog@example.com", "encoded-password", Set.of(role)));

        User persisted = userRepository.findWithRolesAndPrivilegesByEmail("catalog@example.com")
            .orElseThrow();

        assertThat(persisted.getRoles()).singleElement().satisfies(persistedRole -> {
            assertThat(persistedRole.getName()).isEqualTo("ROLE_CATALOG");
            assertThat(persistedRole.getPrivileges()).singleElement()
                .satisfies(persistedPrivilege ->
                    assertThat(persistedPrivilege.getName()).isEqualTo("READ_PRODUCTS"));
        });
    }

    @Test
    void shouldFindUserByEmail() {
        userRepository.saveAndFlush(new User(
            "lookup@example.com", "encoded-password", Set.of()));

        assertThat(userRepository.findByEmail("lookup@example.com"))
            .get()
            .extracting(User::getEmail)
            .isEqualTo("lookup@example.com");
    }
}
