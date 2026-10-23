package io.github.guzillo.fruitifystorebackend.payment.controller;

import com.stripe.model.checkout.Session;
import io.github.guzillo.fruitifystorebackend.payment.dto.CheckoutSessionResponseDTO;
import io.github.guzillo.fruitifystorebackend.payment.dto.CreateCheckoutSessionDTO;
import io.github.guzillo.fruitifystorebackend.payment.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CheckoutService checkoutService;

    @PostMapping("/create-checkout-session")
    public CheckoutSessionResponseDTO createCheckoutSession(Authentication authentication, @Valid @RequestBody CreateCheckoutSessionDTO checkoutSessionDTO) {
         Session checkoutSession = checkoutService.createCheckoutSession(authentication.getName(), checkoutSessionDTO.orderNumber());
         return new CheckoutSessionResponseDTO(
             checkoutSession.getUrl(),
             checkoutSession.getId()
         );
    }
}
