package com.example.rentalmanager.shared.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
            ReactiveUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        var manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            ReactiveAuthenticationManager authenticationManager) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/auth/**").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/webjars/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        // Unit availability management — ADMIN or PROPERTY_MANAGER only
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/units/*/unavailable").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/units/*/available").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.GET, "/api/v1/units/*/unavailability").authenticated()
                        // Unit room gallery — ADMIN or PROPERTY_MANAGER for writes
                        .pathMatchers(HttpMethod.POST,   "/api/v1/units/*/rooms").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.PUT,    "/api/v1/units/*/rooms/*").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/units/*/rooms/*").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.POST,   "/api/v1/units/*/rooms/*/images").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.PUT,    "/api/v1/units/*/rooms/*/images/*").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/units/*/rooms/*/images/*").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        // Maintenance categories — ADMIN or PROPERTY_MANAGER for writes; reads open to all authenticated
                        .pathMatchers(HttpMethod.POST,   "/api/v1/maintenance/categories").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.PUT,    "/api/v1/maintenance/categories/*").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/maintenance/categories/*").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.POST,   "/api/v1/maintenance/categories/*/issues").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.PUT,    "/api/v1/maintenance/categories/*/issues/*").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/maintenance/categories/*/issues/*").hasAnyRole("ADMIN", "PROPERTY_MANAGER")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationWebFilter(jwtAuthenticationConverter), SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    @Bean
    public AuthenticationWebFilter jwtAuthenticationWebFilter(JwtAuthenticationConverter converter) {
        ReactiveAuthenticationManager noopManager = authentication -> Mono.just(authentication);
        var filter = new AuthenticationWebFilter(noopManager);
        filter.setServerAuthenticationConverter(converter);
        return filter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(allowedOrigins, "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
