package io.github.guzillo.fruitifystorebackend.payment.controller;

import com.stripe.model.checkout.Session;
import io.github.guzillo.fruitifystorebackend.payment.dto.CheckoutSessionResponseDTO;
import io.github.guzillo.fruitifystorebackend.payment.dto.CreateCheckoutSessionDTO;
import io.github.guzillo.fruitifystorebackend.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-checkout-session")
    public CheckoutSessionResponseDTO createCheckoutSession(Authentication authentication,
        @Valid @RequestBody CreateCheckoutSessionDTO checkoutSessionDTO) {
        Session checkoutSession = paymentService.createCheckoutSession(authentication.getName(),
            checkoutSessionDTO.orderNumber());
        return new CheckoutSessionResponseDTO(
            checkoutSession.getUrl(),
            checkoutSession.getId()
        );
    }

    @PostMapping("/webhook")
    public void webhook(@RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhookStripe(payload, sigHeader);
    }
}
