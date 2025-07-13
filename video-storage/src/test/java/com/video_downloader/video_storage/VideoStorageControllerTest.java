package com.video_downloader.video_storage;

import com.video_downloader.video_storage.controller.VideoStorageController;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(VideoStorageController.class)
class VideoStorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoStorageController controller;

    @Test
    void shouldReturnVideoWhenFound() throws Exception {
        String url = "https://reddit.com/example";

        // mock answer: video found
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Content-Disposition", "attachment; filename=example.mp4");
            response.getOutputStream().write(new byte[]{1, 2, 3});
            return null;
        }).when(controller).downloadVideo(eq(url), any(HttpServletResponse.class));

        mockMvc.perform(get("/api/videos").param("url", url))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=example.mp4"));
    }

    @Test
    void shouldReturnNotFoundWhenVideoMissing() throws Exception {
        String url = "https://reddit.com/not-found";

        // mock answer: video not found
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }).when(controller).downloadVideo(eq(url), any(HttpServletResponse.class));

        mockMvc.perform(get("/api/videos").param("url", url))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}

