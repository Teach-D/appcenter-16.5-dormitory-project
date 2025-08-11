package com.example.appcenter_project.entity.announcement;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AttachedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileName;

    private Long fileSize; // 파일 크기 (bytes)

    @ManyToOne
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    @Builder
    public AttachedFile(String filePath, String fileName, Long fileSize, Announcement announcement) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.announcement = announcement;
    }
}