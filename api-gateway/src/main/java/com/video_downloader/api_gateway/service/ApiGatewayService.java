package com.video_downloader.api_gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class ApiGatewayService {
    private final WebClient videoStorageClient;

    public ResponseEntity<byte[]> getVideo(String url) {
        try {
            ResponseEntity<byte[]> responseEntity = videoStorageClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("url", url).build())
                .retrieve()
                .toEntity(byte[].class)
                .block();
            if (responseEntity == null || responseEntity.getBody() == null) {
                System.out.println("Null response from video-storage. url=" + url);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String contentDisposition = responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            if (contentDisposition != null) {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            }
            System.out.println("Successfully proxied video. url=" + url);
            return new ResponseEntity<>(responseEntity.getBody(), headers, responseEntity.getStatusCode());
        } catch (WebClientResponseException e) {
            System.out.printf("Error from video-storage. url=%s, status=%s, body=%s\n", url, e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            System.out.println("Unexpected error proxying video. url=" + url);
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 