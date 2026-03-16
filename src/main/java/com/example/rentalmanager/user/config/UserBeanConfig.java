package com.example.rentalmanager.user.config;

import com.example.rentalmanager.user.application.port.output.UserPersistencePort;
import com.example.rentalmanager.user.application.service.UserApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserBeanConfig {

    @Bean
    public UserApplicationService userApplicationService(UserPersistencePort persistencePort,
                                                          PasswordEncoder passwordEncoder) {
        return new UserApplicationService(persistencePort, passwordEncoder);
    }
}
