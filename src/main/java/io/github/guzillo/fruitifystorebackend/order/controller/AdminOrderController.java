package io.github.guzillo.fruitifystorebackend.order.controller;

import io.github.guzillo.fruitifystorebackend.common.pagination.PageableSortValidator;
import io.github.guzillo.fruitifystorebackend.order.dto.AdminOrderPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.service.OrderService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private static final Set<String> SORTABLE_PROPERTIES =
        Set.of("createdAt", "totalAmount", "status");

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderPageResponseDTO findAllOrders(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
        PageableSortValidator.validate(pageable, SORTABLE_PROPERTIES);
        return orderService.findAllOrders(pageable);
    }
}
