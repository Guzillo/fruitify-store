package io.github.guzillo.fruitifystorebackend.health.controller;

import java.security.Principal;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RequestMapping("/api/health")
@RestController
public class HomeController {

    @GetMapping("/home")
    public String home(Principal principal) {
        return "Hello " + principal.getName();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public String user() {
        return "hello user";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin() {
        return "hello admin";
    }

}
