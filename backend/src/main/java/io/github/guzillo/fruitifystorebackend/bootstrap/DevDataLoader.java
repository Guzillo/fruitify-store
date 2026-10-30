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
import java.util.List;
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

            productRepository.saveAll(List.of(
                // Fruits
                product("Bananas", "Ripe yellow bananas, 1 kg pack", "6.49", 60,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365126/bananas.jpg", fruit),
                product("Oranges", "Juicy sweet oranges, 1 kg pack", "7.99", 45,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365126/oranges.jpg", fruit),
                product("Strawberries", "Fresh sweet strawberries, 500 g", "12.90", 30,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365125/strawberries.jpg", fruit),
                product("Mangoes", "Ripe golden mangoes, 2 pieces", "9.99", 20,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365125/mangoes.jpg", fruit),
                product("Pineapple", "Sweet whole pineapple", "11.99", 15,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365124/pineapples.jpg", fruit),
                product("Green grapes", "Seedless green grapes, 500 g", "8.49", 35,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365124/green-grapes.jpg", fruit),
                product("Lemons", "Fresh lemons, 1 kg pack", "5.99", 50,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365126/lemons.jpg", fruit),
                product("Kiwi", "Sweet green kiwi, 4 pieces", "9.49", 40,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365125/kiwi.jpg", fruit),
                product("Pears", "Juicy sweet pears, 1 kg pack", "6.99", 38,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365125/pears.jpg", fruit),

                // Vegetables
                product("Tomatoes", "Vine-ripened tomatoes, 500 g", "8.99", 42,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365123/tomatoes.jpg", vegetables),
                product("Cucumbers", "Crisp garden cucumbers, 2 pieces", "4.49", 55,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365123/cucumbers.jpg", vegetables),
                product("Bell peppers", "Mixed red and yellow bell peppers, 3 pieces", "7.49", 33,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365123/bell-pepers.jpg", vegetables),
                product("Broccoli", "Fresh green broccoli head", "5.99", 28,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365122/broccoli.jpg", vegetables),
                product("Baby spinach", "Tender baby spinach leaves, 200 g", "6.49", 22,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365122/baby-spinach.jpg", vegetables),
                product("Potatoes", "Firm all-purpose potatoes, 2 kg", "4.99", 80,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365122/potatoes.jpg", vegetables),
                product("Yellow onions", "Yellow cooking onions, 1 kg", "3.99", 70,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365121/yellow-onions.jpg", vegetables),
                product("Iceberg lettuce", "Crisp iceberg lettuce head", "4.29", 26,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365122/iceberg-lettuce.jpg", vegetables),
                product("Zucchini", "Fresh green zucchini, 1 kg", "5.49", 24,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365121/zucchini.jpg", vegetables),
                product("Red cabbage", "Fresh red cabbage head", "6.29", 18,
                    "https://res.cloudinary.com/cslyjnry/image/upload/v1793365120/red-cabbage.jpg", vegetables)
            ));
        }
    }

    private static Category category(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        return category;
    }

    private static Product product(String name, String description,
        String price, int stockQuantity, String imageUrl, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stockQuantity);
        product.setIsActive(true);
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        return product;
    }
}
