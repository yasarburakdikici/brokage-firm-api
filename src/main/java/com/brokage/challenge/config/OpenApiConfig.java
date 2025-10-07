package com.brokage.challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {        
        return new OpenAPI()
                .info(new Info()
                        .title("Brokerage Firm Trading API")
                        .version("v1.0")
                        .description("Backend API for managing customer stock orders (Create, List, Delete) and checking asset balances."));
    }
}