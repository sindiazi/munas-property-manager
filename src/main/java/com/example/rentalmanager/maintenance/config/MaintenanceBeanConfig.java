package com.example.rentalmanager.maintenance.config;

import com.example.rentalmanager.maintenance.domain.service.MaintenanceDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration for the Maintenance bounded context. */
@Configuration
public class MaintenanceBeanConfig {

    @Bean
    public MaintenanceDomainService maintenanceDomainService() {
        return new MaintenanceDomainService();
    }
}
