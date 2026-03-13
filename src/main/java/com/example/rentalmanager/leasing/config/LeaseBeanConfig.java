package com.example.rentalmanager.leasing.config;

import com.example.rentalmanager.leasing.domain.service.LeaseDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration for the Leasing bounded context. */
@Configuration
public class LeaseBeanConfig {

    @Bean
    public LeaseDomainService leaseDomainService() {
        return new LeaseDomainService();
    }
}
