package io.github.guzillo.fruitifystorebackend.product.controller;

import io.github.guzillo.fruitifystorebackend.common.pagination.PageableSortValidator;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductPageResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductCreateRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductResponseDTO;
import io.github.guzillo.fruitifystorebackend.product.dto.ProductUpdateRequestDTO;
import io.github.guzillo.fruitifystorebackend.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products",
    description = "Product catalog. Reads are public, writes require the ADMIN role.")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Set<String> SORTABLE_PROPERTIES =
        Set.of("id", "name", "price", "stockQuantity");

    private final ProductService productService;

    @Operation(summary = "List products",
        description = "Paginated product list, optionally filtered by categoryId. "
            + "Inactive products are returned only to ADMIN users. "
            + "Allowed sort properties: id, name, price, stockQuantity.")
    @GetMapping
    public ProductPageResponseDTO getProducts(
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
        Pageable pageable,
        @RequestParam(required = false) Long categoryId,
        Authentication authentication) {
        PageableSortValidator.validate(pageable, SORTABLE_PROPERTIES);
        Page<ProductResponseDTO> products = productService.findProducts(
            pageable, categoryId, isAdmin(authentication));
        return new ProductPageResponseDTO(
            products.getContent(),
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages());
    }

    @Operation(summary = "Get product",
        description = "Inactive products are visible only to ADMIN users.")
    @GetMapping("/{productId}")
    public ProductResponseDTO getProduct(@PathVariable Long productId,
        Authentication authentication) {
        return productService.findProduct(productId, isAdmin(authentication));
    }

    @Operation(summary = "Create product", description = "Requires the ADMIN role.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "409", description = "Product already exists")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO createProduct(@Valid @RequestBody ProductCreateRequestDTO request) {
        return productService.createProduct(request);
    }

    @Operation(summary = "Update product", description = "Requires the ADMIN role.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "404", description = "Product or category not found")
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO updateProduct(@PathVariable Long productId,
        @Valid @RequestBody ProductUpdateRequestDTO request) {
        return productService.updateProduct(productId, request);
    }

    @Operation(summary = "Delete product", description = "Requires the ADMIN role.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Product deleted")
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_ADMIN"));
    }
}
