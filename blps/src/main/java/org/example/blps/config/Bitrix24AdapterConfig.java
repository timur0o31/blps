package org.example.blps.config;

import org.example.bitrix24.api.OrderConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.InitialContext;

@Configuration
public class Bitrix24AdapterConfig {

    @Bean
    public OrderConnectionFactory orderConnectionFactory() throws Exception {
        return (OrderConnectionFactory) new InitialContext()
                .lookup("java:/eis/Bitrix24ConnectionFactory");
    }
}