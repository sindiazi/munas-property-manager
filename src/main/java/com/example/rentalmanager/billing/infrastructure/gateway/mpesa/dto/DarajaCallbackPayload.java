package com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Daraja STK Push callback payload.
 * Structure: Body → stkCallback → { ids, ResultCode, ResultDesc, CallbackMetadata → Item[] }
 * Item.value is Object because Daraja sends String for receipt, Number for amount/date/phone.
 */
public record DarajaCallbackPayload(
        @JsonProperty("Body") Body body
) {
    public record Body(
            @JsonProperty("stkCallback") StkCallback stkCallback
    ) {}

    public record StkCallback(
            @JsonProperty("MerchantRequestID") String merchantRequestId,
            @JsonProperty("CheckoutRequestID") String checkoutRequestId,
            @JsonProperty("ResultCode")        int resultCode,
            @JsonProperty("ResultDesc")        String resultDesc,
            @JsonProperty("CallbackMetadata")  CallbackMetadata callbackMetadata
    ) {}

    public record CallbackMetadata(
            @JsonProperty("Item") List<Item> items
    ) {}

    public record Item(
            @JsonProperty("Name")  String name,
            @JsonProperty("Value") Object value
    ) {}
}
