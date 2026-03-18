package com.example.rentalmanager.billing.infrastructure.gateway.mpesa;

import com.example.rentalmanager.billing.application.port.output.GatewayInitiationResult;
import com.example.rentalmanager.billing.application.port.output.GatewayPaymentRequest;
import com.example.rentalmanager.billing.application.port.output.GatewayPaymentStatus;
import com.example.rentalmanager.billing.application.port.output.GatewayStatusResult;
import com.example.rentalmanager.billing.application.port.output.PaymentGatewayPort;
import com.example.rentalmanager.billing.infrastructure.config.MpesaProperties;
import com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto.DarajaQueryRequest;
import com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto.DarajaQueryResponse;
import com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto.DarajaStkPushRequest;
import com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto.DarajaStkPushResponse;
import com.example.rentalmanager.billing.infrastructure.gateway.mpesa.dto.DarajaTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/** Secondary adapter implementing {@link PaymentGatewayPort} via Safaricom Daraja API. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MpesaDarajaAdapter implements PaymentGatewayPort {

    private final WebClient        mpesaWebClient;
    private final MpesaProperties  props;
    private final DarajaTokenCache tokenCache;

    @Override
    public Mono<GatewayInitiationResult> initiate(GatewayPaymentRequest req) {
        return getValidToken()
                .flatMap(token -> {
                    String timestamp = generateTimestamp();
                    String password  = generatePassword(timestamp);
                    int    amount    = req.amount().setScale(0, RoundingMode.HALF_UP).intValue();

                    var stkRequest = new DarajaStkPushRequest(
                            props.getShortCode(), password, timestamp,
                            props.getTransactionType(), amount,
                            req.phoneNumber(), props.getShortCode(), req.phoneNumber(),
                            props.getCallbackUrl(), req.accountReference(),
                            "Payment for " + req.accountReference());

                    return mpesaWebClient.post()
                            .uri("/mpesa/stkpush/v1/processrequest")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .bodyValue(stkRequest)
                            .retrieve()
                            .bodyToMono(DarajaStkPushResponse.class)
                            .flatMap(resp -> {
                                if (!"0".equals(resp.responseCode())) {
                                    log.error("Daraja STK push rejected — code: {}, desc: {}",
                                            resp.responseCode(), resp.responseDescription());
                                    return Mono.error(new IllegalStateException(
                                            "M-Pesa initiation failed: " + resp.responseDescription()));
                                }
                                return Mono.just(new GatewayInitiationResult(
                                        resp.checkoutRequestId(),
                                        resp.merchantRequestId(),
                                        resp.customerMessage()));
                            });
                });
    }

    @Override
    public Mono<GatewayStatusResult> queryStatus(String checkoutRequestId) {
        return getValidToken()
                .flatMap(token -> {
                    String timestamp = generateTimestamp();
                    String password  = generatePassword(timestamp);

                    var queryRequest = new DarajaQueryRequest(
                            props.getShortCode(), password, timestamp, checkoutRequestId);

                    return mpesaWebClient.post()
                            .uri("/mpesa/stkpushquery/v1/query")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .bodyValue(queryRequest)
                            .retrieve()
                            .bodyToMono(DarajaQueryResponse.class)
                            .map(resp -> {
                                int code = parseResultCode(resp.resultCode());
                                var status = switch (code) {
                                    case 0    -> GatewayPaymentStatus.SUCCESS;
                                    case 1032 -> GatewayPaymentStatus.CANCELLED;
                                    default   -> GatewayPaymentStatus.FAILED;
                                };
                                return new GatewayStatusResult(status, resp.resultDesc(), null, null);
                            });
                });
    }

    private Mono<String> getValidToken() {
        if (tokenCache.isValid()) {
            return Mono.just(tokenCache.getToken());
        }
        String credentials = Base64.getEncoder().encodeToString(
                (props.getConsumerKey() + ":" + props.getConsumerSecret())
                        .getBytes(StandardCharsets.UTF_8));

        return mpesaWebClient.get()
                .uri("/oauth/v1/generate?grant_type=client_credentials")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .retrieve()
                .bodyToMono(DarajaTokenResponse.class)
                .map(resp -> {
                    long expiresIn = Long.parseLong(resp.expiresIn());
                    tokenCache.update(resp.accessToken(), expiresIn);
                    log.debug("Daraja access token refreshed, expires in {}s", expiresIn);
                    return resp.accessToken();
                });
    }

    private String generateTimestamp() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
    }

    private String generatePassword(String timestamp) {
        String raw = props.getShortCode() + props.getPasskey() + timestamp;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private int parseResultCode(String resultCode) {
        try {
            return Integer.parseInt(resultCode);
        } catch (NumberFormatException e) {
            log.warn("Could not parse Daraja ResultCode '{}', treating as failure", resultCode);
            return -1;
        }
    }
}
