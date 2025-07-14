package com.video_downloader.video_storage;

import com.video_downloader.video_storage.controller.VideoStorageController;
import com.video_downloader.video_storage.model.Video;
import com.video_downloader.video_storage.repository.VideoRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VideoStorageApplicationTests {

    @Autowired
    private VideoStorageController controller;

    @Autowired
    private VideoRepository repository;

    private final static String TEST_URL = "https://reddit.com/r/test";
    private final static String TEST_FILENAME = "test.mp4";
    private final static String TEST_PATH = "src/test/resources/test.mp4";
    private final static byte[] TEST_DATA = new byte[]{1, 2, 3};

    @BeforeAll
    static void setUpFile() throws IOException {
        // create fake file with TEST_DATA
        Path path = Paths.get(TEST_PATH);
        Files.createDirectories(path.getParent());
        Files.write(path, TEST_DATA);
    }

    @BeforeEach
    void setUpVideo() {
        Video video = Video.builder()
                .id("id")
                .url(TEST_URL)
                .filename(TEST_FILENAME)
                .path(TEST_PATH)
                .build();
        repository.save(video);
    }

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @AfterAll
    static void deleteFile() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_PATH));
    }

    @Test
    void shouldReturnVideoFromDb() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);

        when(response.getOutputStream()).thenReturn(outputStream);

        controller.downloadVideo(TEST_URL, response);
        verify(response).setContentType("video/mp4");
        verify(response).setHeader(eq("Content-Disposition"), contains(TEST_FILENAME));
        verify(outputStream).write(
                argThat(bytes -> Arrays.equals(Arrays.copyOf(bytes, TEST_DATA.length), TEST_DATA)),
                eq(0),
                eq(TEST_DATA.length)
        );
        verify(outputStream).flush();
    }

}
