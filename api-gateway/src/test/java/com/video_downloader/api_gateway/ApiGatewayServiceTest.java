package com.video_downloader.api_gateway;

import com.video_downloader.api_gateway.service.ApiGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApiGatewayServiceTest {
    @Mock
    private WebClient videoStorageClient;
    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;
    @Mock
    private WebClient.RequestHeadersSpec headersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ApiGatewayService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ApiGatewayService(videoStorageClient);
    }

    @Test
    void getVideo_success() {
        String url = "https://reddit.com/test";
        String traceId = "trace-123";
        byte[] data = new byte[]{1,2,3};
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.mp4")
                .body(data);
        when(videoStorageClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(Mono.just(responseEntity));

        ResponseEntity<byte[]> result = service.getVideo(url, traceId);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("test.mp4");
        assertThat(result.getBody()).isEqualTo(data);
    }

    @Test
    void getVideo_404() {
        String url = "https://reddit.com/test";
        String traceId = "trace-404";
        when(videoStorageClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(Mono.error(
                WebClientResponseException.create(404, "Not Found", HttpHeaders.EMPTY, null, null)));
        ResponseEntity<byte[]> result = service.getVideo(url, traceId);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getVideo_500() {
        String url = "https://reddit.com/test";
        String traceId = "trace-500";
        when(videoStorageClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(Mono.error(
                WebClientResponseException.create(500, "Internal Server Error", HttpHeaders.EMPTY, null, null)));
        ResponseEntity<byte[]> result = service.getVideo(url, traceId);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getVideo_nullResponse() {
        String url = "https://reddit.com/test";
        String traceId = "trace-null";
        when(videoStorageClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(byte[].class)).thenReturn(Mono.justOrEmpty(null));
        ResponseEntity<byte[]> result = service.getVideo(url, traceId);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 