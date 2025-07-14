package com.video_downloader.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.springframework.web.reactive.function.client.ClientRequest;

@Configuration
public class WebClientConfig {

    @Value("${video-storage.base-url}")
    private String videoStorageBaseUrl;

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
    public WebClient videoStorageClient(ExchangeFilterFunction traceIdFilter) {
        int size = 16 * 1024 * 1024; // 16 MB max transfer size

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .baseUrl(videoStorageBaseUrl)
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .filter(traceIdFilter)
                .build();
    }
}
