package org.example.blps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// настройка подключения к камунда
// RestClient — это Spring-класс, который позволяет
// Java-приложению делать HTTP-запросы к другому сервису.

@Configuration
public class CamundaRestClientConfig {

    @Bean
    public RestClient camundaRestClient(
            @Value("${camunda.bpm.client.base-url}") String camundaRestUrl
    ) {
        return RestClient.builder()
                .baseUrl(camundaRestUrl)
                .build();
    }
}