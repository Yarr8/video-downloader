package com.video_downloader.video_storage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public ExchangeFilterFunction traceIdFilter() {
        return (request, next) -> {
            String traceId = org.slf4j.MDC.get("traceId");
            if (traceId != null) {
                return next.exchange(
                    ClientRequest.from(request)
                        .header("X-Request-Id", traceId)
                        .build()
                );
            }
            return next.exchange(request);
        };
    }

    @Bean
    public WebClient webClient(ExchangeFilterFunction traceIdFilter) {
        int size = 16 * 1024 * 1024; // 16MB buffer limit

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .filter(traceIdFilter)
                .build();
    }
}
