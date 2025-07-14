package com.video_downloader.video_storage;

import com.video_downloader.video_storage.model.Video;
import com.video_downloader.video_storage.repository.VideoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VideoRepositoryTest {

    @Autowired
    private VideoRepository repository;

    @Test
    void shouldSaveAndFindVideoByUrl() {
        String url = "https://reddit.com/example";
        Video video = Video.builder()
                .id("id")
                .url(url)
                .filename("file.mp4")
                .path("/tmp/file.mp4")
                .build();

        Optional<Video> found = repository.findByUrl(url);
        assertFalse(found.isPresent());

        repository.save(video);

        found = repository.findByUrl(url);
        assertTrue(found.isPresent());
        assertEquals("file.mp4", found.get().getFilename());
    }
}
