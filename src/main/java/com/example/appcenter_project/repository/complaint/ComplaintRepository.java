package com.example.appcenter_project.repository.complaint;

import com.example.appcenter_project.entity.complaint.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // 특정 사용자의 민원 목록 조회
    List<Complaint> findByUserId(Long userId);
    List<Complaint> findAllByOrderByCreatedDateDesc();

    Optional<Complaint> findByIdAndUserId(Long complaintId, Long userId);

    List<Complaint> findAllByUserIdOrderByCreatedDateDesc(Long user_id);
}
