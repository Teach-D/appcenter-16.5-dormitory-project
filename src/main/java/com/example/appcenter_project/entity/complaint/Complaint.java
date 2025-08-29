package com.example.appcenter_project.entity.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.user.DormType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Complaint extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ComplaintType type; // 유형

    @Enumerated(EnumType.STRING)
    private DormType dormType; // 기숙사

    private String caseNumber; // 사생번호

    private String contact;    // 연락처

    private String title;      // 제목

    private String officer; // 담당자

    @Lob
    private String content;    // 내용

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.PENDING; // 상태 (기본값 대기중)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ComplaintReply reply;

    @OneToMany(orphanRemoval = true)
    private List<Image> imageList = new ArrayList<>();

    @Builder
    public Complaint(ComplaintType type, DormType dormType, String caseNumber,
                     String contact, String title, String content, User user, String officer) {
        this.type = type;
        this.dormType = dormType;
        this.caseNumber = caseNumber;
        this.contact = contact;
        this.title = title;
        this.content = content;
        this.status = ComplaintStatus.PENDING; // 기본 상태
        this.user = user;
        this.officer = officer;
    }

    // 상태 변경
    public void updateStatus(ComplaintStatus status) {
        this.status = status;
    }

    // 답변 연관관계 설정 (편의 메서드)
    public void addReply(ComplaintReply reply) {
        this.reply = reply;
    }

    public void update(RequestComplaintDto dto) {
        this.type = ComplaintType.from(dto.getType());
        this.dormType = DormType.from(dto.getDormType());
        this.caseNumber = dto.getCaseNumber();
        this.contact = dto.getContact();
        this.title = dto.getTitle();
        this.content = dto.getContent();
    }

    public void updateOfficer(String officer) {
        this.officer = officer;
    }
}

