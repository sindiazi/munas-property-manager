package com.example.rentalmanager.tenant.config;

import com.example.rentalmanager.tenant.domain.service.TenantDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration for the Tenant bounded context. */
@Configuration
public class TenantBeanConfig {

    @Bean
    public TenantDomainService tenantDomainService() {
        return new TenantDomainService();
    }
}
