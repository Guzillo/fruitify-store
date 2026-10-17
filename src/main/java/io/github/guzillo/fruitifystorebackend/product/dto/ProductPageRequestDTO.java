package io.github.guzillo.fruitifystorebackend.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class ProductPageRequestDTO {

    @Min(1)
    private Integer page;

    @Min(1)
    @Max(100)
    private Integer size;

    @Pattern(regexp = "name|price", message = "sortBy must be either name or price")
    private String sortBy;

    @Pattern(regexp = "(?i)asc|desc", message = "direction must be asc or desc")
    private String direction = "asc";

    public Pageable toPageable() {
        Sort sort = sortBy == null || sortBy.isBlank()
            ? Sort.unsorted()
            : Sort.by(Sort.Direction.fromString(direction), sortBy);

        return page == null
            ? sort.isSorted() ? Pageable.unpaged(sort) : Pageable.unpaged()
            : PageRequest.of(page - 1, size, sort);
    }

    @AssertTrue(message = "page and size must be provided together")
    public boolean isPaginationComplete() {
        return (page == null) == (size == null);
    }

    @AssertTrue(message = "direction requires sortBy")
    public boolean isDirectionValid() {
        return (sortBy != null && !sortBy.isBlank())
            || "asc".equalsIgnoreCase(direction);
    }
}
