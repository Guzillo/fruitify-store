package io.github.guzillo.fruitifystorebackend.order.controller;

import io.github.guzillo.fruitifystorebackend.common.pagination.PageableSortValidator;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.service.OrderCancellationService;
import io.github.guzillo.fruitifystorebackend.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Orders belonging to the authenticated user")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Set<String> SORTABLE_PROPERTIES = Set.of("createdAt", "totalAmount");

    private final OrderService orderService;
    private final OrderCancellationService orderCancellationService;

    @Operation(summary = "List my orders",
        description = "Paginated list of the current user's orders. "
            + "Allowed sort properties: createdAt, totalAmount.")
    @GetMapping
    public OrderPageResponseDTO findOrders(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,
        Authentication authentication) {
        PageableSortValidator.validate(pageable, SORTABLE_PROPERTIES);
        return orderService.findOrders(authentication.getName(), pageable);
    }

    @Operation(summary = "Create order from cart",
        description = "Creates a PENDING order from the current cart, decreases product stock "
            + "and clears the cart.")
    @ApiResponse(responseCode = "201", description = "Order created")
    @ApiResponse(responseCode = "409", description = "Empty cart, inactive product or insufficient stock")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO createOrder(@Valid @RequestBody OrderCreateRequestDTO request,
        Authentication authentication) {
        return orderService.createOrder(authentication.getName(), request);
    }

    @Operation(summary = "Cancel my unpaid order",
        description = "Cancels a PENDING order, closes its Stripe checkout session and releases "
            + "the reserved stock. Paid orders cannot be cancelled.")
    @ApiResponse(responseCode = "200", description = "Order cancelled")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "409", description = "Order is paid, or a payment is in progress")
    @ApiResponse(responseCode = "503", description = "Payment provider is unavailable")
    @PostMapping("/{orderId}/cancel")
    public OrderResponseDTO cancelOrder(@PathVariable Long orderId,
        Authentication authentication) {
        return orderCancellationService.cancel(authentication.getName(), orderId);
    }

    @Operation(summary = "Get my order")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{orderId}")
    public OrderResponseDTO findOrder(@PathVariable Long orderId,
        Authentication authentication) {
        return orderService.findOrder(authentication.getName(), orderId);
    }
}
