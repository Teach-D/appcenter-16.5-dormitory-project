package com.example.appcenter_project.domain.complaint.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.complaint.dto.request.RequestComplaintReplyDto;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ComplaintReply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String replyTitle;

    @Lob
    private String replyContent;

    private String responderName; // 담당자 이름

    private String attachmentUrl; // 첨부파일 경로

    // 민원 ↔ 답변 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    // 답변 작성자 (관리자: User.role == ROLE_ADMIN)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id")
    private User responder;

    @Builder
    public ComplaintReply(String replyTitle, String replyContent, String responderName, String attachmentUrl, Complaint complaint, User responder) {
        this.replyTitle = replyTitle;
        this.replyContent = replyContent;
        this.responderName = responderName;
        this.attachmentUrl = attachmentUrl;
        this.complaint = complaint;
        this.responder = responder;
    }

    public void update(RequestComplaintReplyDto dto) {
        this.replyTitle = dto.getReplyTitle();
        this.replyContent = dto.getReplyContent();
        this.responderName = dto.getResponderName();
    }
}
