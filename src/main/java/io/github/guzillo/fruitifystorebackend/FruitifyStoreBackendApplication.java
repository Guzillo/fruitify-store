package io.github.guzillo.fruitifystorebackend;

import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import io.github.guzillo.fruitifystorebackend.security.config.RsaKeyProperties;
import io.github.guzillo.fruitifystorebackend.security.repository.RoleRepository;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.util.Set;
import java.math.BigDecimal;
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
        PasswordEncoder passwordEncoder, RoleRepository roleRepository,
        CategoryRepository categoryRepository, ProductRepository productRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(
                    new User("user@email.com", passwordEncoder.encode("pass1"),
                        Set.of(roleRepository.findByName("ROLE_USER"))));
                userRepository.save(
                    new User("admin@email.com", passwordEncoder.encode("pass2"),
                        Set.of(roleRepository.findByName("ROLE_ADMIN"))));
            }

            if (categoryRepository.count() == 0 && productRepository.count() == 0) {
                Category fruit = category(1L, "Fruit", "fruit");
                Category vegetables = category(2L, "Vegetables", "vegetables");
                categoryRepository.saveAll(Set.of(fruit, vegetables));
                productRepository.saveAll(Set.of(
                    product(1L, "Apples", "Crisp red apples", "2.49", 25, fruit),
                    product(2L, "Carrots", "Fresh carrots", "1.79", 40, vegetables)));
            }
        };
    }

    private static Category category(Long id, String name, String slug) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSlug(slug);
        return category;
    }

    private static Product product(Long id, String name, String description,
        String price, int stockQuantity, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stockQuantity);
        product.setIsActive(true);
        product.setCategory(category);
        return product;
    }

}
