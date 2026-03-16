package com.example.rentalmanager.shared.infrastructure.security;

import com.example.rentalmanager.user.application.port.output.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserPersistencePort userPersistencePort;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userPersistencePort.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())
                        .accountLocked(!user.isActive())
                        .disabled(!user.isActive())
                        .build());
    }
}
