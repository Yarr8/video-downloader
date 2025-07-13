package com.video_downloader.video_storage.controller;

import com.video_downloader.video_storage.dto.VideoDownloadRequest;
import com.video_downloader.video_storage.config.DownloaderProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoStorageController {

    private final DownloaderProperties downloaderProperties;
    private final WebClient webClient;

    @GetMapping
    public void downloadVideo(@RequestParam String url, HttpServletResponse response) throws IOException {
        String platform = extractPlatform(url);
        String downloaderUrl = downloaderProperties.getUrls().get(platform);

        if (downloaderUrl == null) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Unsupported platform: " + platform);
            return;
        }

        ResponseEntity<byte[]> responseEntity = webClient.post()
                .uri(downloaderUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new VideoDownloadRequest(url))
                .retrieve()
                .toEntity(byte[].class)
                .block();

        byte[] videoBytes = responseEntity.getBody();

        String contentDisposition = responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        String filename = extractFilenameFromContentDisposition(contentDisposition);

        Path outputPath = Paths.get("videos", filename);
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, videoBytes);

        serveFile(outputPath, response);
    }

    private void serveFile(Path path, HttpServletResponse response) throws IOException {
        response.setContentType("video/mp4");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"");
        StreamUtils.copy(Files.newInputStream(path), response.getOutputStream());
    }

    private String extractPlatform(String url) {
        if (url.contains("reddit.com")) return "reddit";
        if (url.contains("vk.com")) return "vk";
        return "unknown";
    }

    private String extractFilenameFromContentDisposition(String contentDisposition) {
        if (contentDisposition == null) return "default.mp4";

        // i.e: Content-Disposition: attachment; filename="video.mp4"
        for (String part : contentDisposition.split(";")) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                String value = part.substring("filename=".length());
                return value.replaceAll("\"", "");
            }
        }

        return "default.mp4";
    }
}
