package com.example.rentalmanager.shared.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtTokenProvider tokenProvider;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .filter(tokenProvider::validateToken)
                .map(token -> {
                    var authorities = tokenProvider.getAuthoritiesFromToken(token);
                    var userId      = tokenProvider.getUserIdFromToken(token);
                    var username    = tokenProvider.getUsernameFromToken(token);
                    var auth        = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    auth.setDetails(userId);
                    return (Authentication) auth;
                });
    }
}
