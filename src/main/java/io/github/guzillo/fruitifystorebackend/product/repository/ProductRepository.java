package io.github.guzillo.fruitifystorebackend.product.repository;

import io.github.guzillo.fruitifystorebackend.product.model.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);
}
