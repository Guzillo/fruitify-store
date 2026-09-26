package io.github.guzillo.fruitifystorebackend.health.controller;

import java.security.Principal;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
public class HomeController {

    @GetMapping("/api/health/home")
    public String home(Principal principal) {
        return "Hello " + principal.getName();
    }

}
