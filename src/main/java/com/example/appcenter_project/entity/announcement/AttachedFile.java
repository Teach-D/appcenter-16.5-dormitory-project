package com.example.appcenter_project.entity.announcement;

import com.example.appcenter_project.entity.complaint.ComplaintReply;
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

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "complaint_reply_id")
    private ComplaintReply complaintReply;

    @Builder
    public AttachedFile(String filePath, String fileName, Long fileSize, Announcement announcement, ComplaintReply complaintReply) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.announcement = announcement;
        this.complaintReply = complaintReply;
    }
}