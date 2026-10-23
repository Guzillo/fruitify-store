package io.github.guzillo.fruitifystorebackend.order.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.order.model.Order;
import io.github.guzillo.fruitifystorebackend.order.model.OrderItem;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.order.model.ShippingAddress;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import io.github.guzillo.fruitifystorebackend.support.AbstractIntegrationTest;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class OrderRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldPersistOrderWithItemsAndShippingAddress() {
        User user = userRepository.saveAndFlush(
            new User("order-" + UUID.randomUUID() + "@example.com", "password",
                new LinkedHashSet<>()));
        Product product = createProduct();

        Order order = newOrder(newOrderNumber(), user);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setPriceAtPurchase(new BigDecimal("2.49"));
        order.getOrderItems().add(item);

        Long id = orderRepository.saveAndFlush(order).getId();

        Order persisted = orderRepository.findById(id).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(persisted.getTotalAmount()).isEqualByComparingTo("4.98");
        assertThat(persisted.getUser().getId()).isEqualTo(user.getId());
        assertThat(persisted.getShippingAddress().getCity()).isEqualTo("Warsaw");
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getOrderItems()).hasSize(1);
        OrderItem persistedItem = persisted.getOrderItems().iterator().next();
        assertThat(persistedItem.getQuantity()).isEqualTo(2);
        assertThat(persistedItem.getPriceAtPurchase()).isEqualByComparingTo("2.49");
        assertThat(persistedItem.getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    void shouldRejectDuplicateOrderNumber() {
        User user = userRepository.saveAndFlush(
            new User("order-" + UUID.randomUUID() + "@example.com", "password",
                new LinkedHashSet<>()));
        String orderNumber = newOrderNumber();
        orderRepository.saveAndFlush(newOrder(orderNumber, user));

        assertThatThrownBy(() -> orderRepository.saveAndFlush(newOrder(orderNumber, user)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private String newOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 12);
    }

    private Order newOrder(String orderNumber, User user) {
        ShippingAddress address = new ShippingAddress();
        address.setRecipientName("Jan Kowalski");
        address.setStreet("Marszałkowska 1");
        address.setCity("Warsaw");
        address.setPostalCode("00-001");
        address.setCountry("PL");

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("4.98"));
        order.setUser(user);
        order.setShippingAddress(address);
        return order;
    }

    private Product createProduct() {
        Category category = new Category();
        category.setName("Category " + UUID.randomUUID());
        category.setSlug("category-" + UUID.randomUUID());
        category = categoryRepository.saveAndFlush(category);

        Product product = new Product();
        product.setName("Product " + UUID.randomUUID());
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("2.49"));
        product.setStockQuantity(10);
        product.setIsActive(true);
        product.setCategory(category);
        return productRepository.saveAndFlush(product);
    }

    @Test
    void shouldFindOnlyOrdersOfGivenUser() {
        User owner = userRepository.saveAndFlush(
            new User("order-" + UUID.randomUUID() + "@example.com", "password",
                new LinkedHashSet<>()));
        User other = userRepository.saveAndFlush(
            new User("order-" + UUID.randomUUID() + "@example.com", "password",
                new LinkedHashSet<>()));
        String ownOrderNumber = newOrderNumber();
        orderRepository.saveAndFlush(newOrder(ownOrderNumber, owner));
        orderRepository.saveAndFlush(newOrder(newOrderNumber(), other));

        var page = orderRepository.findByUserId(owner.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getOrderNumber()).isEqualTo(ownOrderNumber);
    }

    @Test
    void shouldFindOrderWithItemsOnlyForItsOwner() {
        User owner = userRepository.saveAndFlush(
            new User("order-" + UUID.randomUUID() + "@example.com", "password",
                new LinkedHashSet<>()));
        User other = userRepository.saveAndFlush(
            new User("order-" + UUID.randomUUID() + "@example.com", "password",
                new LinkedHashSet<>()));
        Product product = createProduct();
        Order order = newOrder(newOrderNumber(), owner);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(1);
        item.setPriceAtPurchase(new BigDecimal("2.49"));
        order.getOrderItems().add(item);
        Long id = orderRepository.saveAndFlush(order).getId();

        Order found = orderRepository.findWithItemsByIdAndUserId(id, owner.getId()).orElseThrow();
        assertThat(found.getOrderItems()).hasSize(1);
        assertThat(orderRepository.findWithItemsByIdAndUserId(id, other.getId())).isEmpty();
    }
}
