package com.video_downloader.api_gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableWireMock(
    @ConfigureWireMock(
            port = 8082
))
public class ApiGatewayIntegrationTest {
    @InjectWireMock
    private WireMockServer videoStorageMock;

    private static final String TEST_URL = "https://reddit.com/r/test";

    @Test
    void shouldDownloadVideoThroughGateway() throws Exception {
        byte[] videoData = StreamUtils.copyToByteArray(
                new ClassPathResource("test.mp4").getInputStream());

        videoStorageMock.stubFor(get(urlPathEqualTo("/api/videos"))
                .withQueryParam("url", equalTo(TEST_URL))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "video/mp4")
                        .withHeader("Content-Disposition", "attachment; filename=test.mp4")
                        .withBody(videoData)
                ));

        // simulate GET to api-gateway
        String encodedUrl = java.net.URLEncoder.encode(TEST_URL, "UTF-8");
        URL request = new URL("http://localhost:8080/api/videos?url=" + encodedUrl);
        HttpURLConnection connection = (HttpURLConnection) request.openConnection();
        connection.setRequestMethod("GET");

        // response
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getHeaderField("Content-Type")).isEqualTo("application/octet-stream");


        try (InputStream is = connection.getInputStream()) {
            byte[] responseBody = is.readAllBytes();
            assertThat(responseBody.length).isGreaterThan(0);
        }
    }
}
