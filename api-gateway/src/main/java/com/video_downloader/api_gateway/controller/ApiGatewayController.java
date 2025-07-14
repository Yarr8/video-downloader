package com.video_downloader.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.video_downloader.api_gateway.service.ApiGatewayService;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class ApiGatewayController {

    private final ApiGatewayService apiGatewayService;

    @GetMapping
    public ResponseEntity<byte[]> getVideo(@RequestParam String url) {
        System.out.println("Received video download request. url=" + url);
        return apiGatewayService.getVideo(url);
    }
}
