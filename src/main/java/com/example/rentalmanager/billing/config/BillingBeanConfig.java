package com.example.rentalmanager.billing.config;

import com.example.rentalmanager.billing.infrastructure.config.MpesaProperties;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class BillingBeanConfig {

    @Bean
    public WebClient mpesaWebClient(MpesaProperties props) {
        // Use the JVM DNS resolver instead of Netty's async resolver, which can
        // fail to resolve external hostnames (e.g. sandbox.safaricom.co.ke) in
        // some network environments.
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
