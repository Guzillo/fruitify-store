package io.github.guzillo.fruitifystorebackend.user.repository;

import io.github.guzillo.fruitifystorebackend.user.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.privileges"})
    Optional<User> findWithRolesAndPrivilegesByEmail(String email);

    Optional<User> getUserByEmail(String email);
}
