package com.video_downloader.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.video_downloader.api_gateway.service.ApiGatewayService;

@Slf4j
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class ApiGatewayController {

    private final ApiGatewayService apiGatewayService;

    @GetMapping
    public ResponseEntity<byte[]> getVideo(@RequestParam String url, @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        String traceId = requestId != null ? requestId : java.util.UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        try {
            log.info("Received video download request. url={}, traceId={}", url, traceId);
            return apiGatewayService.getVideo(url, traceId);
        } finally {
            MDC.clear();
        }
    }
}
