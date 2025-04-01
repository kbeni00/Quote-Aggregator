package com.example.qa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class QuoteAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuoteAggregatorApplication.class, args);
    }

    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
