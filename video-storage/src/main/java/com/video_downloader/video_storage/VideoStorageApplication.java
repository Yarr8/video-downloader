package com.video_downloader.video_storage;

import com.video_downloader.video_storage.config.DownloaderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(DownloaderProperties.class)
@SpringBootApplication
public class VideoStorageApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoStorageApplication.class, args);
    }
}
