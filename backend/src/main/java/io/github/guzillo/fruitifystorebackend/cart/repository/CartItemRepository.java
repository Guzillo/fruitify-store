package io.github.guzillo.fruitifystorebackend.cart.repository;

import io.github.guzillo.fruitifystorebackend.cart.model.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItem> findByIdAndCartUserId(Long id, Long userId);

    List<CartItem> findByCartId(Long cartId);

    void deleteByCartId(Long cartId);
}
