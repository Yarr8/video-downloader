package com.video_downloader.video_storage;

import com.video_downloader.video_storage.controller.VideoStorageController;
import com.video_downloader.video_storage.service.VideoStorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoStorageController.class)
class VideoStorageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoStorageService service;

    @Test
    void shouldReturnVideoWhenFound() throws Exception {
        Mockito.doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(2);
            response.setContentType("video/mp4");
            response.setHeader("Content-Disposition", "attachment; filename=example.mp4");
            response.getOutputStream().write(new byte[]{1, 2, 3});
            return null;
        }).when(service).downloadVideo(eq("https://reddit.com/example"), anyString(), any());

        mockMvc.perform(get("/api/videos").param("url", "https://reddit.com/example").header("X-Request-Id", "trace-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=example.mp4"))
                .andExpect(header().string("Content-Type", "video/mp4"));
    }

    @Test
    void shouldReturnNotFoundWhenVideoMissing() throws Exception {
        Mockito.doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(2);
            response.setStatus(404);
            return null;
        }).when(service).downloadVideo(eq("https://reddit.com/not-found"), anyString(), any());

        mockMvc.perform(get("/api/videos").param("url", "https://reddit.com/not-found"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForUnsupportedPlatform() throws Exception {
        Mockito.doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(2);
            response.setStatus(400);
            return null;
        }).when(service).downloadVideo(eq("https://unknown.com/video"), anyString(), any());

        mockMvc.perform(get("/api/videos").param("url", "https://unknown.com/video"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorOnException() throws Exception {
        Mockito.doThrow(new RuntimeException("fail")).when(service).downloadVideo(eq("https://reddit.com/err"), anyString(), any());

        mockMvc.perform(get("/api/videos").param("url", "https://reddit.com/err"))
                .andExpect(status().isInternalServerError());
    }
}
