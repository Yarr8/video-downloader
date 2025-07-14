package com.video_downloader.video_storage.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import com.video_downloader.video_storage.service.VideoStorageService;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoStorageController {

    private final VideoStorageService videoStorageService;

    @GetMapping
    public void downloadVideo(@RequestParam String url, HttpServletResponse response) throws IOException {
        System.out.println("Received video download request. url=" + url);
        videoStorageService.downloadVideo(url, response);
    }
}
