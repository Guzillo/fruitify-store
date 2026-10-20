package io.github.guzillo.fruitifystorebackend.order.repository;

import io.github.guzillo.fruitifystorebackend.order.model.Order;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @NullMarked
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Optional<Order> findWithItemsByIdAndUserId(Long id, Long userId);
}
