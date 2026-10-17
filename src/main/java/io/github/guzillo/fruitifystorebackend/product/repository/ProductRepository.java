package io.github.guzillo.fruitifystorebackend.product.repository;

import io.github.guzillo.fruitifystorebackend.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
