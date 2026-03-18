package com.example.rentalmanager.payment.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Typed binding for the {@code mpesa.*} configuration block. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mpesa")
public class MpesaProperties {
    private String consumerKey;
    private String consumerSecret;
    private String shortCode;
    private String passkey;
    private String callbackUrl;
    private String baseUrl;
    private String transactionType;
}
