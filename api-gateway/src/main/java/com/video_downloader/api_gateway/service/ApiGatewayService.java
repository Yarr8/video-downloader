package com.video_downloader.api_gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiGatewayService {
    private final WebClient videoStorageClient;

    public ResponseEntity<byte[]> getVideo(String url, String traceId) {
        try {
            ResponseEntity<byte[]> responseEntity = videoStorageClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("url", url).build())
                .retrieve()
                .toEntity(byte[].class)
                .block();
            if (responseEntity == null || responseEntity.getBody() == null) {
                log.error("Null response from video-storage. url={}, traceId={}", url, traceId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String contentDisposition = responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            if (contentDisposition != null) {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            }
            log.info("Successfully proxied video. url={}, traceId={}", url, traceId);
            return new ResponseEntity<>(responseEntity.getBody(), headers, responseEntity.getStatusCode());
        } catch (WebClientResponseException e) {
            log.error("Error from video-storage. url={}, traceId={}, status={}, body={}", url, traceId, e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Unexpected error proxying video. url={}, traceId={}", url, traceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 