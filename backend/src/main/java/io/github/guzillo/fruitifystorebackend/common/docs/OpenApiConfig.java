package io.github.guzillo.fruitifystorebackend.common.docs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Fruitify Store API",
        version = "0.0.1",
        description = """
            REST API for an online fruit store. It covers user registration and JWT authentication,
            product and category catalog, shopping cart, order processing and Stripe payments.
            """,
        contact = @Contact(name = "Fruitify Store", url = "https://github.com/guzillo")
    ),
    servers = @Server(url = "/", description = "Current server")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Paste the access token returned by POST /api/auth/signin."
)
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic",
    description = "Email and password used only by POST /api/auth/signin."
)
public class OpenApiConfig {

}
