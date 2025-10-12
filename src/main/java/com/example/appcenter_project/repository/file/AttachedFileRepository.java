package com.example.appcenter_project.repository.file;

import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.file.AttachedFile;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    
    @Modifying
    @Query("DELETE FROM AttachedFile af WHERE af.complaintReply.id = :complaintReplyId")
    int deleteByComplaintReplyId(@Param("complaintReplyId") Long complaintReplyId);
}
