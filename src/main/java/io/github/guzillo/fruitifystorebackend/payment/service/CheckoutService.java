package io.github.guzillo.fruitifystorebackend.payment.service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderItemResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.dto.OrderResponseDTO;
import io.github.guzillo.fruitifystorebackend.order.service.OrderService;
import io.github.guzillo.fruitifystorebackend.payment.config.StripeProperties;
import io.github.guzillo.fruitifystorebackend.payment.error.CheckoutSessionCreationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final OrderService orderService;

    public Session createCheckoutSession(String email, String orderId) {
        String domain = stripeProperties.domain();
        String successUrl = domain + "/checkout?success=true";

        OrderResponseDTO order = orderService.findOrder(email, Long.valueOf(orderId));
        List<OrderItemResponseDTO> orderItems = order.items();
        List<LineItem> lineItems = toLineItems(orderItems);

        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .addAllLineItem(lineItems)
                .build();
        try {
            return stripeClient.v1().checkout().sessions().create(params);
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new CheckoutSessionCreationException();
        }
    }

    private List<LineItem> toLineItems(List<OrderItemResponseDTO> orderItems) {
        return orderItems.stream()
            .map(item -> SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(item.quantity()))
                .setPriceData(
                    PriceData.builder()
                        .setCurrency("pln")
                        .setUnitAmount(toUnitAmount(item.priceAtPurchase()))
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.productName())
                                .build()
                        )
                        .build()
                )
                .build())
            .toList();
    }

    private Long toUnitAmount(BigDecimal price) {
        return price
            .movePointRight(2)
            .setScale(0, RoundingMode.UNNECESSARY)
            .longValueExact();
    }


}