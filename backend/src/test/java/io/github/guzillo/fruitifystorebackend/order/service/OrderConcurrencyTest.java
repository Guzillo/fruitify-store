package io.github.guzillo.fruitifystorebackend.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guzillo.fruitifystorebackend.cart.dto.CartItemCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.cart.exception.InsufficientStockException;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartRepository;
import io.github.guzillo.fruitifystorebackend.cart.service.CartService;
import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.ShippingAddressDTO;
import io.github.guzillo.fruitifystorebackend.order.model.Order;
import io.github.guzillo.fruitifystorebackend.order.repository.OrderRepository;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import io.github.guzillo.fruitifystorebackend.support.AbstractIntegrationTest;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Runs real transactions on separate threads, so it is intentionally not @Transactional.
 * Test data is committed and removed in {@link #cleanUp()}.
 */
class OrderConcurrencyTest extends AbstractIntegrationTest {

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
    private OrderRepository orderRepository;

    private final List<User> users = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        List<Long> userIds = users.stream().map(User::getId).toList();
        orderRepository.findAll().stream()
            .filter(order -> userIds.contains(order.getUser().getId()))
            .forEach(orderRepository::delete);
        for (Long userId : userIds) {
            cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
        }
        userRepository.deleteAll(users);
        productRepository.deleteAll(products);
        categoryRepository.deleteAll(categories);
    }

    @Test
    void shouldLetOnlyOneOrderBuyTheLastUnit() throws Exception {
        Product product = createProduct(1);
        String emailA = createUser();
        String emailB = createUser();
        cartService.addItem(emailA, new CartItemCreateRequestDTO(product.getId(), 1));
        cartService.addItem(emailB, new CartItemCreateRequestDTO(product.getId(), 1));

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<OrderResponseDTO> first = pool.submit(awaitThenOrder(start, emailA));
            Future<OrderResponseDTO> second = pool.submit(awaitThenOrder(start, emailB));
            start.countDown();

            List<Object> outcomes = List.of(outcomeOf(first), outcomeOf(second));

            long succeeded = outcomes.stream().filter(o -> o instanceof OrderResponseDTO).count();
            long rejected = outcomes.stream()
                .filter(o -> o instanceof InsufficientStockException).count();
            assertThat(succeeded).isEqualTo(1);
            assertThat(rejected).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        assertThat(reloaded.getStockQuantity()).isZero();
    }

    private Callable<OrderResponseDTO> awaitThenOrder(CountDownLatch start, String email) {
        return () -> {
            start.await();
            return orderService.createOrder(email, new OrderCreateRequestDTO(
                new ShippingAddressDTO("Jan Kowalski", "Marszałkowska 1", "Warsaw",
                    "00-001", "PL")));
        };
    }

    private Object outcomeOf(Future<OrderResponseDTO> future) throws InterruptedException {
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            return e.getCause();
        } catch (java.util.concurrent.TimeoutException e) {
            throw new IllegalStateException("Order request did not finish in time", e);
        }
    }

    private String createUser() {
        String email = "concurrency-" + UUID.randomUUID() + "@example.com";
        users.add(userRepository.saveAndFlush(
            new User(email, "password", new LinkedHashSet<>())));
        return email;
    }

    private Product createProduct(int stock) {
        Category category = new Category();
        category.setName("Category " + UUID.randomUUID());
        category.setSlug("category-" + UUID.randomUUID());
        category = categoryRepository.saveAndFlush(category);
        categories.add(category);

        Product product = new Product();
        product.setName("Product " + UUID.randomUUID());
        product.setDescription("Concurrency test product");
        product.setPrice(new BigDecimal("2.49"));
        product.setStockQuantity(stock);
        product.setIsActive(true);
        product.setCategory(category);
        product = productRepository.saveAndFlush(product);
        products.add(product);
        return product;
    }
}
