package com.example.rentalmanager.shared.infrastructure.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Reactive WebFilter that logs every inbound HTTP request and its response
 * status + duration.
 *
 * <p>Log format (INFO):
 * <pre>
 *   --> GET /api/v1/tenants
 *   <-- 200 GET /api/v1/tenants (42 ms)
 * </pre>
 */
@Slf4j
@Component
@Order(-100)
public class RequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request  = exchange.getRequest();
        var method   = request.getMethod();
        var path     = request.getPath().value();
        var query    = request.getURI().getRawQuery();
        var fullPath = query != null ? path + "?" + query : path;

        log.info("--> {} {}", method, fullPath);
        long start = System.currentTimeMillis();

        return chain.filter(exchange)
                .doFinally(signal -> {
                    var status  = exchange.getResponse().getStatusCode();
                    long millis = System.currentTimeMillis() - start;
                    log.info("<-- {} {} {} ({} ms)", status, method, fullPath, millis);
                });
    }
}
