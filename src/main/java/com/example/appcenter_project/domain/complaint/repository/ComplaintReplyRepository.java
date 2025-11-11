package com.example.appcenter_project.domain.complaint.repository;

import com.example.appcenter_project.domain.complaint.entity.ComplaintReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintReplyRepository extends JpaRepository<ComplaintReply, Long> {
    void deleteByComplaintId(Long complaintId);
}
