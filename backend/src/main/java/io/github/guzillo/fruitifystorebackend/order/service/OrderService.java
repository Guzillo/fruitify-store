package io.github.guzillo.fruitifystorebackend.order.service;

import io.github.guzillo.fruitifystorebackend.cart.exception.InsufficientStockException;
import io.github.guzillo.fruitifystorebackend.cart.exception.ProductNotActiveException;
import io.github.guzillo.fruitifystorebackend.cart.model.Cart;
import io.github.guzillo.fruitifystorebackend.cart.model.CartItem;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartItemRepository;
import io.github.guzillo.fruitifystorebackend.cart.repository.CartRepository;
import io.github.guzillo.fruitifystorebackend.common.exception.ResourceNotFoundException;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.AdminOrderPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.AdminOrderSummaryDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderSummaryDTO;
import io.github.guzillo.fruitifystorebackend.order.exception.EmptyCartException;
import io.github.guzillo.fruitifystorebackend.order.exception.InvalidOrderStatusTransitionException;
import io.github.guzillo.fruitifystorebackend.order.exception.OrderNotCancellableException;
import io.github.guzillo.fruitifystorebackend.order.mapper.OrderMapper;
import io.github.guzillo.fruitifystorebackend.order.model.Order;
import io.github.guzillo.fruitifystorebackend.order.model.OrderItem;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import io.github.guzillo.fruitifystorebackend.order.repository.OrderRepository;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.product.repository.ProductRepository;
import io.github.guzillo.fruitifystorebackend.user.exception.AuthenticatedUserNotFoundException;
import io.github.guzillo.fruitifystorebackend.user.model.User;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public OrderPageResponseDTO findOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Page<OrderSummaryDTO> page = orderRepository.findByUserId(user.getId(), pageable)
            .map(orderMapper::toSummary);
        return new OrderPageResponseDTO(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages());
    }

    @Transactional
    public OrderResponseDTO changeStatus(Long orderId, OrderStatus target) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() == target) {
            return orderMapper.toResponse(order);
        }
        if (!order.getStatus().canTransitionTo(target)) {
            throw new InvalidOrderStatusTransitionException(order.getStatus(), target);
        }
        if (target == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        order.setStatus(target);
        return orderMapper.toResponse(order);
    }

    /**
     * Cancels a PENDING order whose payment window has passed and gives its stock back.
     * Status is re-checked under the row lock, so an order paid a moment ago is left alone.
     *
     * @return true if the order was cancelled
     */
    @Transactional
    public boolean cancelIfExpired(Long orderId, Instant now) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return false;
        }
        if (order.getCreatedAt().plus(PaymentWindow.ORDER_TTL).isAfter(now)) {
            return false;
        }
        restoreStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        return true;
    }

    @Transactional
    public void attachCheckoutSession(Long orderId, String sessionId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.setStripeSessionId(sessionId);
    }

    @Transactional(readOnly = true)
    public String findCheckoutSessionId(Long orderId) {
        return orderRepository.findById(orderId)
            .map(Order::getStripeSessionId)
            .orElse(null);
    }

    /**
     * Cancels the order only if it is still PENDING, under the row lock, and gives its stock back.
     */
    @Transactional
    public OrderResponseDTO cancelPendingOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return orderMapper.toResponse(order);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderNotCancellableException(orderId,
                "its status is " + order.getStatus());
        }
        restoreStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponse(order);
    }

    private void restoreStock(Order order) {
        List<Long> productIds = order.getOrderItems().stream()
            .map(item -> item.getProduct().getId())
            .toList();
        Map<Long, Product> products = productRepository.findAllByIdInForUpdate(productIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (OrderItem item : order.getOrderItems()) {
            Product product = products.get(item.getProduct().getId());
            if (product == null) {
                throw new ResourceNotFoundException("Product", item.getProduct().getId());
            }
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public AdminOrderPageResponseDTO findAllOrders(Pageable pageable) {
        Page<AdminOrderSummaryDTO> page = orderRepository.findAll(pageable)
            .map(orderMapper::toAdminSummary);
        return new AdminOrderPageResponseDTO(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Order order = orderRepository.findWithItemsByIdAndUserId(orderId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponseDTO createOrder(String email, OrderCreateRequestDTO request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(EmptyCartException::new);
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        Map<Long, Product> products = lockProducts(cartItems);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);
        order.setShippingAddress(orderMapper.toEntity(request.shippingAddress()));

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProduct().getId();
            Product product = products.get(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Product", productId);
            }
            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new ProductNotActiveException(productId);
            }
            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(productId, product.getStockQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            order.getOrderItems().add(orderItem);

            total = total.add(product.getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByCartId(cart.getId());

        return orderMapper.toResponse(saved);
    }

    private Map<Long, Product> lockProducts(List<CartItem> cartItems) {
        List<Long> productIds = cartItems.stream()
            .map(cartItem -> cartItem.getProduct().getId())
            .toList();
        return productRepository.findAllByIdInForUpdate(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "")
            .substring(0, 12).toUpperCase();
    }
}
