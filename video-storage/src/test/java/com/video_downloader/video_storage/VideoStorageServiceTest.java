package com.video_downloader.video_storage;

import com.video_downloader.video_storage.config.DownloaderProperties;
import com.video_downloader.video_storage.service.VideoStorageService;
import com.video_downloader.video_storage.model.Video;
import com.video_downloader.video_storage.repository.VideoRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VideoStorageServiceTest {
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private DownloaderProperties downloaderProperties;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec uriSpec;
    @Mock
    private WebClient.RequestBodySpec bodySpec;
    @Mock
    private WebClient.RequestHeadersSpec headersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private HttpServletResponse response;

    private VideoStorageService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VideoStorageService(videoRepository, downloaderProperties, webClient);
    }

    @Test
    void downloadVideo_foundLocally() throws Exception {
        Video video = Video.builder().id("id").url("url").filename("file.mp4").path("src/test/resources/test.mp4").build();
        when(videoRepository.findByUrl(anyString())).thenReturn(Optional.of(video));
        Files.write(Path.of("src/test/resources/test.mp4"), new byte[]{1,2,3});
        ServletOutputStreamStub out = new ServletOutputStreamStub();
        when(response.getOutputStream()).thenReturn(out);
        service.downloadVideo("url", "traceId", response);
        assertThat(out.getBytes()).containsExactly(1,2,3);
        assertThat(out.isFlushed()).isTrue();
        Files.deleteIfExists(Path.of("src/test/resources/test.mp4"));
    }

    @Test
    void downloadVideo_notFound_downloadsAndSaves() throws Exception {
        when(videoRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(downloaderProperties.getUrls()).thenReturn(Map.of("reddit", "http://downloader"));
        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        ResponseEntity<byte[]> resp = ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dl.mp4").body(new byte[]{4,5,6});
        when(responseSpec.toEntity(byte[].class)).thenReturn(Mono.just(resp));
        ServletOutputStreamStub out = new ServletOutputStreamStub();
        when(response.getOutputStream()).thenReturn(out);
        service.downloadVideo("https://reddit.com/abc", "traceId", response);
        assertThat(out.getBytes()).containsExactly(4,5,6);
        assertThat(out.isFlushed()).isTrue();
    }

    @Test
    void downloadVideo_unsupportedPlatform() throws Exception {
        when(videoRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(downloaderProperties.getUrls()).thenReturn(Map.of("reddit", "http://downloader"));
        when(response.getOutputStream()).thenReturn(new ServletOutputStreamStub());
        service.downloadVideo("https://unknown.com/abc", "traceId", response);
        verify(response).sendError(eq(HttpStatus.BAD_REQUEST.value()), contains("Unsupported platform"));
    }

    @Test
    void downloadVideo_downloaderError() throws Exception {
        when(videoRepository.findByUrl(anyString())).thenReturn(Optional.empty());
        when(downloaderProperties.getUrls()).thenReturn(Map.of("reddit", "http://downloader"));
        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(Mono.error(WebClientResponseException.create(500, "fail", HttpHeaders.EMPTY, null, null)));
        when(response.getOutputStream()).thenReturn(new ServletOutputStreamStub());
        service.downloadVideo("https://reddit.com/abc", "traceId", response);
        verify(response).sendError(eq(500), anyString());
    }


// Stub for ServletOutputStream
class ServletOutputStreamStub extends jakarta.servlet.ServletOutputStream {
    private final java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    private boolean flushed = false;
    @Override
    public void write(int b) { baos.write(b); }
    @Override
    public void flush() { flushed = true; }
    @Override
    public boolean isReady() { return true; }
    @Override
    public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
    public byte[] getBytes() { return baos.toByteArray(); }
    public boolean isFlushed() { return flushed; }
    }
}
