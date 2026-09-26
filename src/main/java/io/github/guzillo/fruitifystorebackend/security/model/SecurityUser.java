package io.github.guzillo.fruitifystorebackend.security.model;

import io.github.guzillo.fruitifystorebackend.user.model.User;
import java.util.Arrays;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final User user;

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(user.getRoles()
            .split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    @NullMarked
    public String getUsername() {
        return user.getEmail();
    }
}
