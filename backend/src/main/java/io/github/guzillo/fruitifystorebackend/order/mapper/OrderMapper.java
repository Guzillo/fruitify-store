package io.github.guzillo.fruitifystorebackend.order.mapper;

import io.github.guzillo.fruitifystorebackend.order.dto.AdminOrderSummaryDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderSummaryDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.ShippingAddressDTO;
import io.github.guzillo.fruitifystorebackend.order.model.Order;
import io.github.guzillo.fruitifystorebackend.order.model.OrderItem;
import io.github.guzillo.fruitifystorebackend.order.model.ShippingAddress;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public ShippingAddress toEntity(ShippingAddressDTO dto) {
        ShippingAddress address = new ShippingAddress();
        address.setRecipientName(dto.recipientName());
        address.setStreet(dto.street());
        address.setCity(dto.city());
        address.setPostalCode(dto.postalCode());
        address.setCountry(dto.country());
        return address;
    }

    public OrderResponseDTO toResponse(Order order) {
        ShippingAddress address = order.getShippingAddress();
        return new OrderResponseDTO(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getTotalAmount(),
            new ShippingAddressDTO(
                address.getRecipientName(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry()),
            order.getOrderItems().stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .map(item -> new OrderItemResponseDTO(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getPriceAtPurchase(),
                    item.getQuantity()))
                .toList(),
            order.getCreatedAt());
    }

    public OrderSummaryDTO toSummary(Order order) {
        return new OrderSummaryDTO(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt());
    }

    public AdminOrderSummaryDTO toAdminSummary(Order order) {
        return new AdminOrderSummaryDTO(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUser().getId(),
            order.getUser().getEmail());
    }
}
