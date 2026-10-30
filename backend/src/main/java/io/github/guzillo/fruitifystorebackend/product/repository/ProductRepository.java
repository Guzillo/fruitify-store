package io.github.guzillo.fruitifystorebackend.product.repository;

import io.github.guzillo.fruitifystorebackend.product.model.Product;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("select case when count(oi) > 0 then true else false end "
        + "from OrderItem oi where oi.product.id = :productId")
    boolean isInAnyOrder(@Param("productId") Long productId);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryByIdAndIsActiveTrue(Long id);

    @EntityGraph(attributePaths = "category")
    @NullMarked
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @NullMarked
    Page<Product> findAllByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findAllByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids order by p.id")
    List<Product> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
