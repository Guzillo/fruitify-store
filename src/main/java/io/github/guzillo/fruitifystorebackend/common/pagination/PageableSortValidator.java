package io.github.guzillo.fruitifystorebackend.common.pagination;

import io.github.guzillo.fruitifystorebackend.common.exception.InvalidSortPropertyException;
import java.util.Collection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableSortValidator {

    private PageableSortValidator() {
    }

    public static void validate(Pageable pageable, Collection<String> allowedProperties) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new InvalidSortPropertyException(order.getProperty());
            }
        }
    }
}
