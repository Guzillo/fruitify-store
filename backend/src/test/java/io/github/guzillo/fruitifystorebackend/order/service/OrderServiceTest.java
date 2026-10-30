package io.github.guzillo.fruitifystorebackend.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.guzillo.fruitifystorebackend.cart.exception.InsufficientStockException;
import io.github.guzillo.fruitifystorebackend.cart.exception.ProductNotActiveException;
import io.github.guzillo.fruitifystorebackend.cart.model.Cart;
import io.github.guzillo.fruitifystorebackend.cart.model.CartItem;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartItemRepository;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartRepository;
import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.order.dto.AdminOrderPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.ShippingAddressDTO;
import io.github.guzillo.fruitifystorebackend.order.exception.EmptyCartException;
import io.github.guzillo.fruitifystorebackend.order.model.Order;
import io.github.guzillo.fruitifystorebackend.order.mapper.OrderMapper;
import io.github.guzillo.fruitifystorebackend.order.exception.InvalidOrderStatusTransitionException;
import io.github.guzillo.fruitifystorebackend.order.model.OrderItem;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.order.model.ShippingAddress;
import io.github.guzillo.fruitifystorebackend.order.model.ShippingAddress;
import io.github.guzillo.fruitifystorebackend.order.repository.OrderRepository;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import io.github.guzillo.fruitifystorebackend.user.exception.AuthenticatedUserNotFoundException;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String EMAIL = "user@example.com";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(EMAIL);
        setId(user, 1L);

        cart = new Cart();
        cart.setUser(user);
        setId(cart, 10L);

        product = new Product();
        product.setName("Apple");
        product.setPrice(new BigDecimal("2.49"));
        product.setStockQuantity(10);
        product.setIsActive(true);
        setId(product, 100L);

        cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);
        setId(cartItem, 50L);
    }

    @Test
    void shouldCreatePendingOrderFromCartAndDecreaseStock() {
        stubCartWith(List.of(cartItem), List.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            setId(saved, 200L);
            return saved;
        });

        OrderResponseDTO response = orderService.createOrder(EMAIL, request());

        assertThat(response.id()).isEqualTo(200L);
        assertThat(response.orderNumber()).startsWith("ORD-");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo("7.47");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productId()).isEqualTo(100L);
        assertThat(response.items().get(0).priceAtPurchase()).isEqualByComparingTo("2.49");
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(product.getStockQuantity()).isEqualTo(7);
        verify(cartItemRepository).deleteByCartId(10L);
    }

    @Test
    void shouldCancelUnpaidOrderAfterPaymentWindowAndRestoreStock() {
        Order order = pendingOrderCreatedAt(Instant.now().minusSeconds(3 * 3600L));
        when(orderRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(order));
        when(productRepository.findAllByIdInForUpdate(any())).thenReturn(List.of(product));

        assertThat(orderService.cancelIfExpired(200L, Instant.now())).isTrue();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStockQuantity()).isEqualTo(13);
    }

    @Test
    void shouldLeavePaidOrderAloneWhenExpiryRuns() {
        Order order = pendingOrderCreatedAt(Instant.now().minusSeconds(3 * 3600L));
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(order));

        assertThat(orderService.cancelIfExpired(200L, Instant.now())).isFalse();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(productRepository, never()).findAllByIdInForUpdate(any());
    }

    @Test
    void shouldKeepRecentPendingOrderWhenExpiryRuns() {
        Order order = pendingOrderCreatedAt(Instant.now().minusSeconds(60L));
        when(orderRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(order));

        assertThat(orderService.cancelIfExpired(200L, Instant.now())).isFalse();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    private Order pendingOrderCreatedAt(Instant createdAt) {
        Order order = new Order();
        setId(order, 200L);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(createdAt);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(3);
        order.getOrderItems().add(item);
        return order;
    }

    @Test
    void shouldThrowWhenCartHasNoItems() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, request()))
            .isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowWhenUserHasNoCart() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, request()))
            .isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowWhenProductIsInactive() {
        product.setIsActive(false);
        stubCartWith(List.of(cartItem), List.of(product));

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, request()))
            .isInstanceOf(ProductNotActiveException.class);

        assertThat(product.getStockQuantity()).isEqualTo(10);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowWhenStockIsInsufficient() {
        product.setStockQuantity(2);
        stubCartWith(List.of(cartItem), List.of(product));

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, request()))
            .isInstanceOf(InsufficientStockException.class);

        assertThat(product.getStockQuantity()).isEqualTo(2);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowWhenProductDisappearedBeforeLocking() {
        stubCartWith(List.of(cartItem), List.of());

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, request()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldThrowWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(EMAIL, request()))
            .isInstanceOf(AuthenticatedUserNotFoundException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldReturnOrderWithItemsForOwner() {
        Order order = new Order();
        order.setOrderNumber("ORD-ABCDEF123456");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("7.47"));
        order.setShippingAddress(new ShippingAddress());
        order.getShippingAddress().setRecipientName("Jan Kowalski");
        order.getShippingAddress().setStreet("Marszałkowska 1");
        order.getShippingAddress().setCity("Warsaw");
        order.getShippingAddress().setPostalCode("00-001");
        order.getShippingAddress().setCountry("PL");
        setId(order, 200L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findWithItemsByIdAndUserId(200L, 1L)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.findOrder(EMAIL, 200L);

        assertThat(response.id()).isEqualTo(200L);
        assertThat(response.orderNumber()).isEqualTo("ORD-ABCDEF123456");
        assertThat(response.shippingAddress().city()).isEqualTo("Warsaw");
    }

    @Test
    void shouldThrowWhenOrderDoesNotExistOrBelongsToAnotherUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findWithItemsByIdAndUserId(200L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findOrder(EMAIL, 200L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Order");
    }

    @Test
    void shouldThrowWhenAuthenticatedUserDoesNotExistOnFindOrder() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findOrder(EMAIL, 200L))
            .isInstanceOf(AuthenticatedUserNotFoundException.class);

        verify(orderRepository, never()).findWithItemsByIdAndUserId(any(), any());
    }

    @Test
    void shouldReturnAllOrdersWithCustomerEmailForAdmin() {
        Order order = new Order();
        order.setOrderNumber("ORD-ABCDEF123456");
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(new BigDecimal("7.47"));
        order.setUser(user);
        setId(order, 200L);

        when(orderRepository.findAll(PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1));

        AdminOrderPageResponseDTO page = orderService.findAllOrders(PageRequest.of(0, 10));

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).customerEmail()).isEqualTo(EMAIL);
        assertThat(page.content().get(0).userId()).isEqualTo(1L);
        assertThat(page.content().get(0).status()).isEqualTo(OrderStatus.PAID);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    private void stubCartWith(List<CartItem> items, List<Product> lockedProducts) {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L)).thenReturn(items);
        when(productRepository.findAllByIdInForUpdate(List.of(100L))).thenReturn(lockedProducts);
    }

    private static OrderCreateRequestDTO request() {
        return new OrderCreateRequestDTO(new ShippingAddressDTO(
            "Jan Kowalski", "Marszałkowska 1", "Warsaw", "00-001", "PL"));
    }

    private static void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void shouldReturnPageOfOrderSummariesForUser() {
        Order order = new Order();
        order.setOrderNumber("ORD-ABCDEF123456");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("7.47"));
        setId(order, 200L);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserId(1L, PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1));

        OrderPageResponseDTO page = orderService.findOrders(EMAIL, PageRequest.of(0, 10));

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().orderNumber()).isEqualTo("ORD-ABCDEF123456");
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.number()).isZero();
        assertThat(page.size()).isEqualTo(10);
    }

    @Test
    void shouldThrowWhenAuthenticatedUserDoesNotExistOnFindOrders() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findOrders(EMAIL, PageRequest.of(0, 10)))
            .isInstanceOf(AuthenticatedUserNotFoundException.class);

        verify(orderRepository, never()).findByUserId(any(), any());
    }

    @Test
    void shouldMarkPendingOrderAsPaidWithoutTouchingStock() {
        Order order = orderInStatus(OrderStatus.PENDING);
        when(orderRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.changeStatus(200L, OrderStatus.PAID);

        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(product.getStockQuantity()).isEqualTo(7);
        verify(productRepository, never()).findAllByIdInForUpdate(any());
    }

    @Test
    void shouldCancelPendingOrderAndRestoreStock() {
        Order order = orderInStatus(OrderStatus.PENDING);
        when(orderRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(order));
        when(productRepository.findAllByIdInForUpdate(List.of(100L))).thenReturn(List.of(product));

        OrderResponseDTO response = orderService.changeStatus(200L, OrderStatus.CANCELLED);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void shouldRejectChangeFromCancelledWithoutTouchingStock() {
        Order order = orderInStatus(OrderStatus.CANCELLED);
        when(orderRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.changeStatus(200L, OrderStatus.PAID))
            .isInstanceOf(InvalidOrderStatusTransitionException.class);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void shouldThrowWhenOrderToChangeDoesNotExist() {
        when(orderRepository.findByIdForUpdate(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.changeStatus(200L, OrderStatus.PAID))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Order");
    }

    private Order orderInStatus(OrderStatus status) {
        product.setStockQuantity(7);
        Order order = new Order();
        order.setOrderNumber("ORD-ABCDEF123456");
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("7.47"));
        order.setUser(user);
        order.setShippingAddress(new ShippingAddress());
        setId(order, 200L);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(3);
        item.setPriceAtPurchase(new BigDecimal("2.49"));
        order.getOrderItems().add(item);
        return order;
    }
}
