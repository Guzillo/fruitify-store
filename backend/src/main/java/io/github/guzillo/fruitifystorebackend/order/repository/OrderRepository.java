package io.github.guzillo.fruitifystorebackend.order.repository;

import io.github.guzillo.fruitifystorebackend.order.model.Order;
import io.github.guzillo.fruitifystorebackend.order.model.OrderStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @NullMarked
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Optional<Order> findWithItemsByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    @Query("select o.id from Order o where o.status = :status and o.createdAt <= :createdBefore")
    List<Long> findIdsByStatusAndCreatedAtBefore(@Param("status") OrderStatus status,
        @Param("createdBefore") Instant createdBefore);
}
