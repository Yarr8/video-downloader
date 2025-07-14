package com.video_downloader.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class ApiGatewayController {

    private final WebClient videoStorageClient;

    @GetMapping
    public ResponseEntity<byte[]> getVideo(@RequestParam String url) {
        System.out.println("Downloading: " + url);
        ResponseEntity<byte[]> responseEntity = videoStorageClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("url", url)
                .build())
            .retrieve()
            .toEntity(byte[].class)
            .block();

        if (responseEntity == null || responseEntity.getBody() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        String contentDisposition = responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        if (contentDisposition != null) {
            headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        }

        return new ResponseEntity<>(responseEntity.getBody(), headers, responseEntity.getStatusCode());
    }
}
