package io.github.guzillo.fruitifystorebackend.bootstrap;

import io.github.guzillo.fruitifystorebackend.cart.model.Cart;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartRepository;
import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import io.github.guzillo.fruitifystorebackend.security.repository.RoleRepository;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    @Override
    public void run(String... args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            saveUserWithCart(new User("user@email.com", passwordEncoder.encode("pass1"),
                Set.of(roleRepository.findByName("ROLE_USER"))));
            saveUserWithCart(new User("admin@email.com", passwordEncoder.encode("pass2"),
                Set.of(roleRepository.findByName("ROLE_ADMIN"))));
        }
    }

    private void saveUserWithCart(User user) {
        Cart cart = new Cart();
        cart.setUser(userRepository.save(user));
        cartRepository.save(cart);
    }

    private void seedProducts() {
        if (categoryRepository.count() == 0 && productRepository.count() == 0) {
            Category fruit = categoryRepository.save(category("Fruit", "fruit"));
            Category vegetables = categoryRepository.save(category("Vegetables", "vegetables"));
            productRepository.saveAll(Set.of(
                product("Apples", "Crisp red apples", "2.49", 25, fruit),
                product("Carrots", "Fresh carrots", "1.79", 40, vegetables)));
        }
    }

    private static Category category(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        return category;
    }

    private static Product product(String name, String description,
        String price, int stockQuantity, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stockQuantity);
        product.setIsActive(true);
        product.setCategory(category);
        return product;
    }
}
