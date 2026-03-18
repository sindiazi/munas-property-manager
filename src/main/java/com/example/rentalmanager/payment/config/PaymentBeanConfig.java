package com.example.rentalmanager.payment.config;

import com.example.rentalmanager.payment.domain.service.PaymentDomainService;
import com.example.rentalmanager.payment.infrastructure.config.MpesaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/** Spring configuration for the Payment bounded context. */
@Configuration
public class PaymentBeanConfig {

    @Bean
    public PaymentDomainService paymentDomainService() {
        return new PaymentDomainService();
    }

    @Bean
    public WebClient mpesaWebClient(MpesaProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
