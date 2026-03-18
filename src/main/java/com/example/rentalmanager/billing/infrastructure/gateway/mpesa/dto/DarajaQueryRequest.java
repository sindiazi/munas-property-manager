package com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DarajaQueryRequest(
        @JsonProperty("BusinessShortCode")  String businessShortCode,
        @JsonProperty("Password")           String password,
        @JsonProperty("Timestamp")          String timestamp,
        @JsonProperty("CheckoutRequestID")  String checkoutRequestId
) {}
