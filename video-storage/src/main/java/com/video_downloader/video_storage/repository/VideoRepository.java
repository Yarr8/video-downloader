package com.video_downloader.video_storage.repository;

import com.video_downloader.video_storage.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByUrl(String url);
}
