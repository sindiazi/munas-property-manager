package com.example.rentalmanager.payment.infrastructure.web.controller;

import com.example.rentalmanager.payment.application.dto.command.InitiateMpesaPaymentCommand;
import com.example.rentalmanager.payment.application.dto.command.MpesaCallbackCommand;
import com.example.rentalmanager.payment.application.dto.response.MpesaInitiationResponse;
import com.example.rentalmanager.payment.application.dto.response.MpesaStatusResponse;
import com.example.rentalmanager.payment.application.port.input.HandleMpesaCallbackUseCase;
import com.example.rentalmanager.payment.application.port.input.InitiateMpesaPaymentUseCase;
import com.example.rentalmanager.payment.application.port.input.QueryMpesaStatusUseCase;
import com.example.rentalmanager.payment.infrastructure.gateway.mpesa.dto.DarajaCallbackPayload;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/** Primary adapter for M-Pesa payment operations. */
@Slf4j
@Tag(name = "M-Pesa Payments", description = "M-Pesa STK Push payment initiation and status")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class MpesaPaymentController {

    private final InitiateMpesaPaymentUseCase initiateUseCase;
    private final HandleMpesaCallbackUseCase  callbackUseCase;
    private final QueryMpesaStatusUseCase     queryUseCase;

    @Operation(summary = "Initiate an M-Pesa STK Push payment")
    @PostMapping("/mpesa")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<MpesaInitiationResponse> initiate(
            @Valid @RequestBody InitiateMpesaPaymentCommand command) {
        return initiateUseCase.initiateMpesaPayment(command);
    }

    @Operation(summary = "Daraja callback endpoint — called by Safaricom on payment completion")
    @PostMapping("/mpesa/callback")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> callback(@RequestBody DarajaCallbackPayload payload) {
        var stkCallback = payload.body().stkCallback();
        MpesaCallbackCommand cmd = assembleCallbackCommand(stkCallback);

        return callbackUseCase.handleCallback(cmd)
                .onErrorResume(e -> {
                    log.warn("M-Pesa callback processing error (responding 200 to Daraja): {}",
                            e.getMessage());
                    return Mono.empty();
                });
    }

    @Operation(summary = "Query M-Pesa transaction status for a payment")
    @GetMapping("/{id}/mpesa/status")
    public Mono<MpesaStatusResponse> status(@PathVariable UUID id) {
        return queryUseCase.queryStatus(id);
    }

    // ── helpers ────────────────────────────────────────────────────────────

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

    /**
     * Daraja sends TransactionDate as a long in yyyyMMddHHmmss format.
     * Converts to Unix epoch seconds.
     */
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
