package com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DarajaQueryResponse(
        @JsonProperty("ResponseCode")         String responseCode,
        @JsonProperty("ResultCode")           String resultCode,
        @JsonProperty("ResultDesc")           String resultDesc,
        @JsonProperty("MerchantRequestID")    String merchantRequestId,
        @JsonProperty("CheckoutRequestID")    String checkoutRequestId
) {}
