package com.example.appcenter_project.repository.announcement;

import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.announcement.AttachedFile;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachedFileRepository extends JpaRepository<AttachedFile, Long> {
    List<AttachedFile> findByAnnouncement(Announcement announcement);
    void deleteByFilePath(String filePath);
    void deleteByAnnouncement(Announcement announcement);

    Optional<AttachedFile> findByFilePath(String filePath);

    Optional<AttachedFile> findByFilePathAndAnnouncementId(String filePath, Long announcementId);

    Optional<AttachedFile> findByFilePathAndAnnouncement(String filePath, Announcement announcement);

    List<AttachedFile> findByComplaintReply(ComplaintReply complaintReply);

    void deleteByComplaintReply(ComplaintReply complaintReply);
    
    // ID 기반 메서드 추가
    List<AttachedFile> findByComplaintReplyId(Long complaintReplyId);
    
    void deleteByComplaintReplyId(Long complaintReplyId);
}
