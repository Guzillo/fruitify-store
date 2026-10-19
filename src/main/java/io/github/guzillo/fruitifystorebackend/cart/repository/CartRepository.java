package io.github.guzillo.fruitifystorebackend.cart.repository;

import io.github.guzillo.fruitifystorebackend.cart.model.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    Optional<Cart> findWithItemsByUserId(Long userId);
}
