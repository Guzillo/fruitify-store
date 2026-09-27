package io.github.guzillo.fruitifystorebackend;

import io.github.guzillo.fruitifystorebackend.security.config.RsaKeyProperties;
import io.github.guzillo.fruitifystorebackend.security.repository.RoleRepository;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
public class FruitifyStoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitifyStoreBackendApplication.class, args);
    }

    @Profile("dev")
    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository,
        PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(
                    new User("user@email.com", passwordEncoder.encode("pass1"), Set.of(roleRepository.findByName("ROLE_USER"))));
                userRepository.save(
                    new User("admin@email.com", passwordEncoder.encode("pass2"), Set.of(roleRepository.findByName("ROLE_ADMIN"))));
            }
        };
    }

}
