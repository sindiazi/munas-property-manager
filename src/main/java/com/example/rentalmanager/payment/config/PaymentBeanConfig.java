package com.example.rentalmanager.payment.config;

import com.example.rentalmanager.payment.domain.service.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration for the Payment bounded context. */
@Configuration
public class PaymentBeanConfig {

    @Bean
    public PaymentDomainService paymentDomainService() {
        return new PaymentDomainService();
    }
}
