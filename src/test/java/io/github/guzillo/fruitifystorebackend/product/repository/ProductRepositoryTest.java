package io.github.guzillo.fruitifystorebackend.product.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guzillo.fruitifystorebackend.product.model.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldPersistAndLoadProductWithAllFields() {
        Product product = product(1001L, "Apples");

        productRepository.saveAndFlush(product);

        Product persisted = productRepository.findById(1001L).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("Apples");
        assertThat(persisted.getDescription()).isEqualTo("Fresh apples");
        assertThat(persisted.getPrice()).isEqualByComparingTo("2.49");
        assertThat(persisted.getStockQuantity()).isEqualTo(25);
        assertThat(persisted.getIsActive()).isTrue();
        assertThat(persisted.getImageUrl()).isEqualTo("https://example.test/apples.png");
    }

    @Test
    void shouldRejectDuplicateProductName() {
        productRepository.saveAndFlush(product(1001L, "Apples"));

        assertThatThrownBy(() -> productRepository.saveAndFlush(product(1002L, "Apples")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Fresh apples");
        product.setPrice(new BigDecimal("2.49"));
        product.setStockQuantity(25);
        product.setIsActive(true);
        product.setImageUrl("https://example.test/apples.png");
        return product;
    }
}
