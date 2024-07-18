package com.bezkoder.springjwt.config;

import com.bezkoder.springjwt.service.CognitoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public CognitoService cognitoService() {
        return new CognitoService();
    }

}
