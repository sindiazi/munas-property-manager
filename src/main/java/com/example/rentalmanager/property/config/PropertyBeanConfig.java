package com.example.rentalmanager.property.config;

import com.example.rentalmanager.property.domain.service.PropertyDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the Property bounded context.
 *
 * <p>Domain services are not Spring-managed by design (they are pure POJOs);
 * they are exposed as beans here so they can be injected where needed without
 * coupling the domain layer to Spring annotations.
 */
@Configuration
public class PropertyBeanConfig {

    @Bean
    public PropertyDomainService propertyDomainService() {
        return new PropertyDomainService();
    }
}
