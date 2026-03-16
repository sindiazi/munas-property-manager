package com.example.rentalmanager.settings.config;

import com.example.rentalmanager.settings.application.port.output.UserSettingsPersistencePort;
import com.example.rentalmanager.settings.application.service.UserSettingsApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettingsBeanConfig {

    @Bean
    public UserSettingsApplicationService userSettingsApplicationService(UserSettingsPersistencePort persistencePort) {
        return new UserSettingsApplicationService(persistencePort);
    }
}
