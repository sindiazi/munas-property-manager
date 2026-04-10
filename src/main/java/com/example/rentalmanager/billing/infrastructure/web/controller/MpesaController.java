package com.example.rentalmanager.billing.infrastructure.web.controller;

import com.example.rentalmanager.billing.application.dto.command.InitiateMpesaPaymentCommand;
import com.example.rentalmanager.billing.application.dto.command.MpesaCallbackCommand;
import com.example.rentalmanager.billing.application.dto.response.MpesaInitiationResponse;
import com.example.rentalmanager.billing.application.dto.response.MpesaStatusResponse;
import com.example.rentalmanager.billing.application.port.input.HandleMpesaCallbackUseCase;
import com.example.rentalmanager.billing.application.port.input.InitiateMpesaPaymentUseCase;
import com.example.rentalmanager.billing.application.port.input.QueryMpesaStatusUseCase;
import com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto.DarajaCallbackPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "M-Pesa Payments", description = "M-Pesa STK Push payment initiation and status")
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class MpesaController {

    private final InitiateMpesaPaymentUseCase initiateUseCase;
    private final HandleMpesaCallbackUseCase  callbackUseCase;
    private final QueryMpesaStatusUseCase     queryUseCase;

    @Operation(summary = "Initiate an M-Pesa STK Push payment for an invoice")
    @PostMapping("/{id}/payments/mpesa")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<MpesaInitiationResponse> initiate(
            @PathVariable UUID id,
            @Valid @RequestBody InitiateMpesaPaymentCommand command) {
        var merged = new InitiateMpesaPaymentCommand(id, command.amount(), command.phoneNumber());
        return initiateUseCase.initiateMpesaPayment(merged);
    }

    @Operation(summary = "Daraja callback endpoint — called by Safaricom on payment completion")
    @PostMapping("/payments/mpesa/callback")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> callback(@RequestBody DarajaCallbackPayload payload) {
        var stkCallback = payload.body().stkCallback();
        MpesaCallbackCommand cmd = assembleCallbackCommand(stkCallback);

        return callbackUseCase.handleCallback(cmd)
                .onErrorResume(e -> {
                    log.error("M-Pesa callback processing failed for CheckoutRequestID '{}' (responding 200 to Daraja): {}",
                            stkCallback.checkoutRequestId(), e.getMessage(), e);
                    return Mono.empty();
                });
    }

    @Operation(summary = "Query M-Pesa transaction status for a payment transaction")
    @GetMapping("/payments/mpesa/{paymentTransactionId}/status")
    public Mono<MpesaStatusResponse> status(@PathVariable UUID paymentTransactionId) {
        return queryUseCase.queryStatus(paymentTransactionId);
    }

    private MpesaCallbackCommand assembleCallbackCommand(DarajaCallbackPayload.StkCallback cb) {
        List<DarajaCallbackPayload.Item> items =
                cb.callbackMetadata() != null && cb.callbackMetadata().items() != null
                        ? cb.callbackMetadata().items()
                        : List.of();

        String     receiptNumber   = null;
        BigDecimal amount          = null;
        String     phoneNumber     = null;
        Instant    transactionDate = null;

        for (var item : items) {
            switch (item.name()) {
                case "MpesaReceiptNumber" -> receiptNumber = String.valueOf(item.value());
                case "Amount"             -> amount = new BigDecimal(item.value().toString());
                case "PhoneNumber"        -> phoneNumber = String.valueOf(
                        ((Number) item.value()).longValue());
                case "TransactionDate"    -> {
                    long epochSeconds = parseDarajaDate(item.value().toString());
                    transactionDate   = Instant.ofEpochSecond(epochSeconds);
                }
                default -> { /* ignore */ }
            }
        }

        return new MpesaCallbackCommand(
                cb.checkoutRequestId(), cb.merchantRequestId(),
                cb.resultCode(), cb.resultDesc(),
                receiptNumber, amount, phoneNumber, transactionDate);
    }

    private long parseDarajaDate(String raw) {
        try {
            var formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            var ldt = java.time.LocalDateTime.parse(raw, formatter);
            return ldt.toEpochSecond(java.time.ZoneOffset.UTC);
        } catch (Exception e) {
            log.warn("Could not parse Daraja TransactionDate '{}', using current time", raw);
            return Instant.now().getEpochSecond();
        }
    }
}
