package io.github.guzillo.fruitifystorebackend.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.model.Cart;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartItemRepository;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartRepository;
import io.github.guzillo.fruitifystorebackend.cart.service.CartService;
import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.ShippingAddressDTO;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import io.github.guzillo.fruitifystorebackend.support.AbstractIntegrationTest;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class OrderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void shouldCreateOrderFromCartDecreaseStockAndClearCart() {
        String email = "order-" + UUID.randomUUID() + "@example.com";
        User user = userRepository.saveAndFlush(
            new User(email, "password", new LinkedHashSet<>()));
        Product product = createProduct(10);
        cartService.addItem(email, new CartItemCreateRequestDTO(product.getId(), 3));

        OrderResponseDTO response = orderService.createOrder(email, new OrderCreateRequestDTO(
            new ShippingAddressDTO("Jan Kowalski", "Marszałkowska 1", "Warsaw", "00-001", "PL")));

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo("7.47");
        assertThat(response.items()).hasSize(1);

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(7);

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(cartItemRepository.findByCartId(cart.getId())).isEmpty();
    }

    private Product createProduct(int stock) {
        Category category = new Category();
        category.setName("Category " + UUID.randomUUID());
        category.setSlug("category-" + UUID.randomUUID());
        category = categoryRepository.saveAndFlush(category);

        Product product = new Product();
        product.setName("Product " + UUID.randomUUID());
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("2.49"));
        product.setStockQuantity(stock);
        product.setIsActive(true);
        product.setCategory(category);
        return productRepository.saveAndFlush(product);
    }

    @Test
    void shouldRestoreStockWhenOrderIsCancelled() {
        String email = "order-" + UUID.randomUUID() + "@example.com";
        userRepository.saveAndFlush(new User(email, "password", new LinkedHashSet<>()));
        Product product = createProduct(10);
        cartService.addItem(email, new CartItemCreateRequestDTO(product.getId(), 3));
        OrderResponseDTO order = orderService.createOrder(email, new OrderCreateRequestDTO(
            new ShippingAddressDTO("Jan Kowalski", "Marszałkowska 1", "Warsaw", "00-001", "PL")));

        orderService.changeStatus(order.id(), OrderStatus.CANCELLED);

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isEqualTo(10);
    }
}
