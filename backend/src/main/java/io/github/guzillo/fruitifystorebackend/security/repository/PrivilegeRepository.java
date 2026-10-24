package io.github.guzillo.fruitifystorebackend.security.repository;

import io.github.guzillo.fruitifystorebackend.security.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Privilege findByName(String name);
}
