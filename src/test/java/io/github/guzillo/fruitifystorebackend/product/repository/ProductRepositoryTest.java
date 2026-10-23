package io.github.guzillo.fruitifystorebackend.product.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guzillo.fruitifystorebackend.category.Category;
import io.github.guzillo.fruitifystorebackend.category.CategoryRepository;
import io.github.guzillo.fruitifystorebackend.product.model.Product;
import io.github.guzillo.fruitifystorebackend.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Sql(statements = "TRUNCATE TABLE products, categories RESTART IDENTITY CASCADE",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFetchProductWithCategory() {
        Category category = new Category();
        category.setName("Fruit");
        category.setSlug("fruit");
        category = categoryRepository.saveAndFlush(category);

        Product product = product("Apples");
        product.setCategory(category);
        Product persistedProduct = productRepository.saveAndFlush(product);

        Product persisted = productRepository.findWithCategoryById(persistedProduct.getId())
            .orElseThrow();

        assertThat(persisted.getCategory().getName()).isEqualTo("Fruit");
    }

    @Test
    void shouldPersistAndLoadProductWithAllFields() {
        Product product = product("Apples");

        Product persistedProduct = productRepository.saveAndFlush(product);

        Product persisted = productRepository.findById(persistedProduct.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo("Apples");
        assertThat(persisted.getDescription()).isEqualTo("Fresh apples");
        assertThat(persisted.getPrice()).isEqualByComparingTo("2.49");
        assertThat(persisted.getStockQuantity()).isEqualTo(25);
        assertThat(persisted.getIsActive()).isTrue();
        assertThat(persisted.getImageUrl()).isEqualTo("https://example.test/apples.png");
    }

    @Test
    void shouldRejectDuplicateProductName() {
        productRepository.saveAndFlush(product("Apples"));

        assertThatThrownBy(() -> productRepository.saveAndFlush(product("Apples")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Product product(String name) {
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() -> {
            Category created = new Category();
            created.setName("Test category");
            created.setSlug("test-category");
            return categoryRepository.save(created);
        });
        Product product = new Product();
        product.setName(name);
        product.setDescription("Fresh apples");
        product.setPrice(new BigDecimal("2.49"));
        product.setStockQuantity(25);
        product.setIsActive(true);
        product.setImageUrl("https://example.test/apples.png");
        product.setCategory(category);
        return product;
    }
}
