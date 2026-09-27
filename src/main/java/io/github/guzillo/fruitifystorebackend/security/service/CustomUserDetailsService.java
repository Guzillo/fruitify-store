package io.github.guzillo.fruitifystorebackend.security.service;

import io.github.guzillo.fruitifystorebackend.security.model.Privilege;
import io.github.guzillo.fruitifystorebackend.security.model.Role;
import io.github.guzillo.fruitifystorebackend.security.model.SecurityUser;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements
    org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@org.jspecify.annotations.NonNull String username)
        throws UsernameNotFoundException {
        return userRepository
            .findByEmail(username)
            .map(SecurityUser::new)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    }


}
