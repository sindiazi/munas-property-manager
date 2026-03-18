package com.example.rentalmanager.billing.infrastructure.gateway.mpesa;

import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Simple in-memory cache for the Daraja OAuth access token.
 * Two concurrent cache misses may both fetch — that is fine; both tokens are valid for an hour
 * and last-write-wins.
 */
@Component
public class DarajaTokenCache {

    private volatile String  cachedToken;
    private volatile Instant expiresAt = Instant.EPOCH;

    public boolean isValid() {
        return cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(60));
    }

    public String getToken() {
        return cachedToken;
    }

    public void update(String token, long expiresInSeconds) {
        this.cachedToken = token;
        this.expiresAt   = Instant.now().plusSeconds(expiresInSeconds);
    }
}
