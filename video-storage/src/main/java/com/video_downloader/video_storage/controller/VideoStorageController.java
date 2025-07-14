package com.video_downloader.video_storage.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import com.video_downloader.video_storage.service.VideoStorageService;

@Slf4j
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoStorageController {

    private final VideoStorageService videoStorageService;

    @GetMapping
    public void downloadVideo(@RequestParam String url, @RequestHeader(value = "X-Request-Id", required = false) String requestId, HttpServletResponse response) throws IOException {
        String traceId = requestId != null ? requestId : java.util.UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        try {
            log.info("Received video download request. url={}, traceId={}", url, traceId);
            videoStorageService.downloadVideo(url, traceId, response);
        } finally {
            MDC.clear();
        }
    }
}
