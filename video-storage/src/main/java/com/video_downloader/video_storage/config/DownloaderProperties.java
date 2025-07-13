package com.video_downloader.video_storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "downloader")
@Getter
@Setter
public class DownloaderProperties {
    private Map<String, String> urls;
}
