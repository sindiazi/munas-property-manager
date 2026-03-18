package com.example.rentalmanager.payment.infrastructure.gateway.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DarajaStkPushRequest(
        @JsonProperty("BusinessShortCode") String businessShortCode,
        @JsonProperty("Password")          String password,
        @JsonProperty("Timestamp")         String timestamp,
        @JsonProperty("TransactionType")   String transactionType,
        @JsonProperty("Amount")            int amount,
        @JsonProperty("PartyA")            String partyA,
        @JsonProperty("PartyB")            String partyB,
        @JsonProperty("PhoneNumber")       String phoneNumber,
        @JsonProperty("CallBackURL")       String callBackUrl,
        @JsonProperty("AccountReference")  String accountReference,
        @JsonProperty("TransactionDesc")   String transactionDesc
) {}
