package com.example.appcenter_project.common.file.entity;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.complaint.entity.ComplaintReply;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor
public abstract class AttachedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileName;

    private Long fileSize; // 파일 크기 (bytes)

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "complaint_reply_id")
    private ComplaintReply complaintReply;

    public AttachedFile(String filePath, String fileName, Long fileSize, Announcement announcement, ComplaintReply complaintReply) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.announcement = announcement;
        this.complaintReply = complaintReply;
    }

    public void updateAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }
}