package com.video_downloader.video_storage.service;

import com.video_downloader.video_storage.dto.VideoDownloadRequest;
import com.video_downloader.video_storage.model.Video;
import com.video_downloader.video_storage.repository.VideoRepository;
import com.video_downloader.video_storage.config.DownloaderProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoStorageService {
    private final VideoRepository videoRepository;
    private final DownloaderProperties downloaderProperties;
    private final WebClient webClient;

    public void downloadVideo(String url, HttpServletResponse response) throws IOException {
        Optional<Video> existing = videoRepository.findByUrl(url);
        if (existing.isPresent()) {
            Path videoPath = Path.of(existing.get().getPath());
            if (Files.exists(videoPath)) {
                System.out.printf("Serving video from local storage. url=%s, path=%s\n", url, videoPath);
                serveFile(videoPath, response);
                return;
            }
        }
        System.out.println("File not found locally. Requesting downloader. url=" + url);
        String id = UUID.randomUUID().toString();
        String platform = extractPlatform(url);
        String downloaderUrl = downloaderProperties.getUrls().get(platform);
        if (downloaderUrl == null) {
            System.out.println("Unsupported platform. url=" + url);
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Unsupported platform: " + platform);
            return;
        }
        try {
            ResponseEntity<byte[]> responseEntity = webClient.post()
                    .uri(downloaderUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new VideoDownloadRequest(url))
                    .retrieve()
                    .toEntity(byte[].class)
                    .block();
            if (responseEntity == null || responseEntity.getBody() == null) {
                System.out.println("Null response from downloader. url=" + url);
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Downloader returned no data");
                return;
            }
            byte[] videoBytes = responseEntity.getBody();
            String contentDisposition = responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            String filename = extractFilenameFromContentDisposition(contentDisposition);
            Path outputPath = Paths.get("videos", filename);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, videoBytes);
            Video video = Video.builder()
                    .id(id)
                    .url(url)
                    .filename(filename)
                    .path(outputPath.toAbsolutePath().toString())
                    .build();
            videoRepository.save(video);
            System.out.printf("Saved new video. url=%s, filename=%s\n", url, filename);
            serveFile(outputPath, response);
        } catch (WebClientResponseException e) {
            System.out.printf("Downloader error. url=%s, status=%s, body=%s\n", url, e.getStatusCode(), e.getResponseBodyAsString());
            response.sendError(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("Unexpected error. url=" + url);
            System.out.println(e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    private void serveFile(Path path, HttpServletResponse response) throws IOException {
        response.setContentType("video/mp4");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"");
        try (var is = Files.newInputStream(path)) {
            StreamUtils.copy(is, response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    private String extractPlatform(String url) {
        if (url.contains("reddit.com")) return "reddit";
        if (url.contains("vk.com")) return "vk";
        return "unknown";
    }

    private String extractFilenameFromContentDisposition(String contentDisposition) {
        if (contentDisposition == null) return "default.mp4";
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