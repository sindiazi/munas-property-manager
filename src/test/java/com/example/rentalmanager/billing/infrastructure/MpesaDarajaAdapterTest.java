package com.example.rentalmanager.billing.infrastructure;

import com.example.rentalmanager.billing.application.port.output.GatewayPaymentRequest;
import com.example.rentalmanager.billing.application.port.output.GatewayPaymentStatus;
import com.example.rentalmanager.billing.infrastructure.config.MpesaProperties;
import com.example.rentalmanager.billing.infrastructure.gateway.mpesa.DarajaTokenCache;
import com.example.rentalmanager.billing.infrastructure.gateway.mpesa.MpesaDarajaAdapter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MpesaDarajaAdapterTest {

    private MockWebServer    server;
    private MpesaDarajaAdapter adapter;
    private DarajaTokenCache   tokenCache;
    private MpesaProperties    props;

    private static final String SHORT_CODE = "174379";
    private static final String PASSKEY    =
            "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        props = new MpesaProperties();
        props.setConsumerKey("test_key");
        props.setConsumerSecret("test_secret");
        props.setShortCode(SHORT_CODE);
        props.setPasskey(PASSKEY);
        props.setCallbackUrl("https://example.com/callback");
        props.setBaseUrl(server.url("/").toString());
        props.setTransactionType("CustomerPayBillOnline");

        tokenCache = new DarajaTokenCache();

        WebClient webClient = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        adapter = new MpesaDarajaAdapter(webClient, props, tokenCache);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    // ── Token fetching ──────────────────────────────────────────────────────

    @Test
    void initiate_whenTokenCacheEmpty_fetchesTokenThenSendsRequest() throws Exception {
        server.enqueue(tokenResponse("access_token_abc", "3600"));
        server.enqueue(stkPushResponse("ws_CO_123", "MR_456", "0", "Success"));

        var req = testRequest();
        StepVerifier.create(adapter.initiate(req))
                .assertNext(result -> {
                    assertThat(result.gatewayTransactionId()).isEqualTo("ws_CO_123");
                    assertThat(result.merchantRequestId()).isEqualTo("MR_456");
                })
                .verifyComplete();

        // First request should be the OAuth token fetch
        var tokenRequest = server.takeRequest();
        assertThat(tokenRequest.getPath()).contains("/oauth/v1/generate");
        assertThat(tokenRequest.getHeader(HttpHeaders.AUTHORIZATION)).startsWith("Basic ");

        // Second request should be the STK push
        var pushRequest = server.takeRequest();
        assertThat(pushRequest.getPath()).contains("/mpesa/stkpush/v1/processrequest");
    }

    @Test
    void initiate_whenTokenCacheValid_skipsTokenFetch() throws Exception {
        tokenCache.update("cached_token_xyz", 3600);
        server.enqueue(stkPushResponse("ws_CO_CACHED", "MR_CACHED", "0", "OK"));

        StepVerifier.create(adapter.initiate(testRequest()))
                .assertNext(r -> assertThat(r.gatewayTransactionId()).isEqualTo("ws_CO_CACHED"))
                .verifyComplete();

        // Only one request should have been made (STK push, not token)
        assertThat(server.getRequestCount()).isEqualTo(1);
        var req = server.takeRequest();
        assertThat(req.getPath()).contains("/stkpush");
    }

    // ── Amount rounding ─────────────────────────────────────────────────────

    @Test
    void initiate_amount_isRoundedToWholeKES_inSerializedBody() throws Exception {
        tokenCache.update("tok", 3600);
        server.enqueue(stkPushResponse("ws_CO_AMT", "MR_AMT", "0", "OK"));

        var req = new GatewayPaymentRequest(UUID.randomUUID(), UUID.randomUUID(),
                "254708374149", new BigDecimal("1500.75"), "KES", "INV-ABCD1234");

        StepVerifier.create(adapter.initiate(req)).expectNextCount(1).verifyComplete();

        var recordedReq = server.takeRequest();
        var body = recordedReq.getBody().readUtf8();
        // Daraja Amount must be the integer 1501 (rounded HALF_UP from 1500.75)
        assertThat(body).contains("\"Amount\":1501");
    }

    // ── Password encoding ───────────────────────────────────────────────────

    @Test
    void initiate_password_isBase64OfShortCodePasskeyTimestamp() throws Exception {
        tokenCache.update("tok", 3600);
        server.enqueue(stkPushResponse("ws_CO_PWD", "MR_PWD", "0", "OK"));

        StepVerifier.create(adapter.initiate(testRequest())).expectNextCount(1).verifyComplete();

        var body = server.takeRequest().getBody().readUtf8();
        // Extract Timestamp from request and verify Password = base64(shortCode+passkey+timestamp)
        var timestampMatch = body.replaceAll(".*\"Timestamp\":\"(\\d{14})\".*", "$1");
        if (!timestampMatch.equals(body)) {
            var expected = Base64.getEncoder().encodeToString(
                    (SHORT_CODE + PASSKEY + timestampMatch).getBytes());
            assertThat(body).contains("\"Password\":\"" + expected + "\"");
        }
    }

    // ── Error handling ──────────────────────────────────────────────────────

    @Test
    void initiate_daraja500_propagatesError() {
        tokenCache.update("tok", 3600);
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));

        StepVerifier.create(adapter.initiate(testRequest()))
                .expectError()
                .verify();
    }

    // ── queryStatus ─────────────────────────────────────────────────────────

    @Test
    void queryStatus_resultCode0_mapsToSuccess() throws Exception {
        tokenCache.update("tok", 3600);
        server.enqueue(queryResponse("0", "The service request is processed successfully.",
                "ws_CO_DONE", "MR_DONE"));

        StepVerifier.create(adapter.queryStatus("ws_CO_DONE"))
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(GatewayPaymentStatus.SUCCESS);
                    assertThat(result.resultDescription()).contains("processed successfully");
                })
                .verifyComplete();

        var req = server.takeRequest();
        assertThat(req.getPath()).contains("/stkpushquery/v1/query");
    }

    @Test
    void queryStatus_resultCode1032_mapsToCancelled() throws Exception {
        tokenCache.update("tok", 3600);
        server.enqueue(queryResponse("1032", "Request cancelled by user.", "ws_CO_CAN", "MR_CAN"));

        StepVerifier.create(adapter.queryStatus("ws_CO_CAN"))
                .assertNext(result -> assertThat(result.status())
                        .isEqualTo(GatewayPaymentStatus.CANCELLED))
                .verifyComplete();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private GatewayPaymentRequest testRequest() {
        return new GatewayPaymentRequest(UUID.randomUUID(), UUID.randomUUID(),
                "254708374149", new BigDecimal("5000"), "KES", "INV-ABCD1234");
    }

    private MockResponse tokenResponse(String token, String expiresIn) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"access_token\":\"" + token + "\",\"expires_in\":\"" + expiresIn + "\"}");
    }

    private MockResponse stkPushResponse(String checkoutId, String merchantId,
                                         String responseCode, String description) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"MerchantRequestID\":\"" + merchantId + "\"," +
                         "\"CheckoutRequestID\":\"" + checkoutId + "\"," +
                         "\"ResponseCode\":\"" + responseCode + "\"," +
                         "\"ResponseDescription\":\"" + description + "\"," +
                         "\"CustomerMessage\":\"Success. Request accepted for processing\"}");
    }

    private MockResponse queryResponse(String resultCode, String resultDesc,
                                       String checkoutId, String merchantId) {
        return new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"ResponseCode\":\"0\"," +
                         "\"ResultCode\":\"" + resultCode + "\"," +
                         "\"ResultDesc\":\"" + resultDesc + "\"," +
                         "\"MerchantRequestID\":\"" + merchantId + "\"," +
                         "\"CheckoutRequestID\":\"" + checkoutId + "\"}");
    }
}
