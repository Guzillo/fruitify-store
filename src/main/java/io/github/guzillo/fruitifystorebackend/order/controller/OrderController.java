package io.github.guzillo.fruitifystorebackend.order.controller;

import io.github.guzillo.fruitifystorebackend.common.pagination.PageableSortValidator;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.service.OrderService;
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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Set<String> SORTABLE_PROPERTIES = Set.of("createdAt", "totalAmount");

    private final OrderService orderService;

    @GetMapping
    public OrderPageResponseDTO findOrders(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,
        Authentication authentication) {
        PageableSortValidator.validate(pageable, SORTABLE_PROPERTIES);
        return orderService.findOrders(authentication.getName(), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO createOrder(@Valid @RequestBody OrderCreateRequestDTO request,
        Authentication authentication) {
        return orderService.createOrder(authentication.getName(), request);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDTO findOrder(@PathVariable Long orderId,
        Authentication authentication) {
        return orderService.findOrder(authentication.getName(), orderId);
    }
}
