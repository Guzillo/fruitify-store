package io.github.guzillo.fruitifystorebackend.payment.controller;

import com.stripe.model.checkout.Session;
import io.github.guzillo.fruitifystorebackend.payment.dto.CheckoutSessionResponseDTO;
import io.github.guzillo.fruitifystorebackend.payment.dto.CreateCheckoutSessionDTO;
import io.github.guzillo.fruitifystorebackend.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments", description = "Stripe Checkout integration")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create Stripe Checkout session",
        description = "Creates a Stripe Checkout session for a PENDING order owned by the "
            + "authenticated user. Returns the hosted payment page URL.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Checkout session created")
    @ApiResponse(responseCode = "409", description = "Order is not payable")
    @ApiResponse(responseCode = "503", description = "Stripe is unavailable")
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

    @Operation(summary = "Stripe webhook",
        description = "Handles the checkout.session.completed event and marks the order as PAID. "
            + "Requires a valid Stripe-Signature header.")
    @ApiResponse(responseCode = "200", description = "Event processed")
    @ApiResponse(responseCode = "400", description = "Invalid Stripe signature")
    @PostMapping("/webhook")
    public void webhook(@RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhookStripe(payload, sigHeader);
    }
}
