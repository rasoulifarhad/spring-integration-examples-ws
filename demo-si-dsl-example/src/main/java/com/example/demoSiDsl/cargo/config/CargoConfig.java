package com.example.demoSiDsl.cargo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
@Profile("cargo")
public class CargoConfig {
   
    @Bean
    public MessageChannel cargoGWDefaultRequestChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel cargoSplitterOutputChannel() {
        return MessageChannels.direct().get();
    }
    
    @Bean
    public MessageChannel cargoFilterDiscardChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel cargoFilterOutputChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel cargoRouterDomesticOutputChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel cargoRouterInternationalOutputChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel cargoTransformerOutputChannel() {
        return MessageChannels.direct().get();
    }
    
}
