package com.video_downloader.video_storage.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    private String id;  // UUID

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String path;
}
