package io.github.guzillo.fruitifystorebackend.security.service;

import io.github.guzillo.fruitifystorebackend.security.model.SecurityUser;
import io.github.guzillo.fruitifystorebackend.user.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Primary
public class CustomUserDetailsService implements
    org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@org.jspecify.annotations.NonNull String username)
        throws UsernameNotFoundException {
        return userRepository
            .findWithRolesAndPrivilegesByEmail(username)
            .map(SecurityUser::new)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    }


}
