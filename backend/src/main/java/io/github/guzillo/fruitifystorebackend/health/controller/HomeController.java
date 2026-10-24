package io.github.guzillo.fruitifystorebackend.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health (dev)", description = "Development-only diagnostic endpoints.")
@SecurityRequirement(name = "bearerAuth")
@Profile("dev")
@RequestMapping("/api/health")
@RestController
public class HomeController {

    @Operation(summary = "Greet the authenticated user")
    @GetMapping("/home")
    public String home(Principal principal) {
        return "Hello " + principal.getName();
    }

    @Operation(summary = "Check USER role")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public String user() {
        return "hello user";
    }

    @Operation(summary = "Check ADMIN role")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin() {
        return "hello admin";
    }

}
