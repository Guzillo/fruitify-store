package io.github.guzillo.fruitifystorebackend.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.model.checkout.Session;
import io.github.guzillo.fruitifystorebackend.payment.controller.PaymentController;
import io.github.guzillo.fruitifystorebackend.payment.dto.CheckoutSessionResponseDTO;
import io.github.guzillo.fruitifystorebackend.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService checkoutService;

    @Mock
    private Session checkoutSession;

    private PaymentController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentController(checkoutService);
    }

    @Test
    void shouldCreateCheckoutSessionForAuthenticatedUser() {
        Authentication authentication =
            new UsernamePasswordAuthenticationToken("customer@example.com", null);
        when(checkoutService.createCheckoutSession("customer@example.com", "123"))
            .thenReturn(checkoutSession);
        when(checkoutSession.getUrl()).thenReturn("https://checkout.example/session");
        when(checkoutSession.getId()).thenReturn("cs_test_123");

        CheckoutSessionResponseDTO response =
            controller.createCheckoutSession(authentication,
                new io.github.guzillo.fruitifystorebackend.payment.dto.CreateCheckoutSessionDTO("123"));

        assertThat(response.url()).isEqualTo("https://checkout.example/session");
        assertThat(response.sessionId()).isEqualTo("cs_test_123");
        verify(checkoutService).createCheckoutSession("customer@example.com", "123");
    }
}
