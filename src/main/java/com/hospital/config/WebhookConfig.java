package com.hospital.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebhookConfig {

    @Value("${webhook.timeout-ms:5000}")
    private int timeoutMs;

    /**
     * RestTemplate for webhook calls with explicit connect + read timeouts.
     *
     * RestTemplateBuilder.connectTimeout(Duration) was removed in Spring Boot 3.
     * The correct Spring Boot 3 approach is to configure the ClientHttpRequestFactory
     * directly and pass it to the builder.
     */
    @Bean(name = "webhookRestTemplate")
    public RestTemplate webhookRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}
