package com.example.appcenter_project.domain.complaint.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.complaint.dto.request.RequestComplaintDto;
import com.example.appcenter_project.domain.complaint.enums.ComplaintStatus;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.complaint.enums.ComplaintType;
import com.example.appcenter_project.domain.complaint.enums.DormBuilding;
import com.example.appcenter_project.domain.user.enums.DormType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Complaint extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private ComplaintType type; // 유형

    @Enumerated(EnumType.STRING)
    private DormType dormType; // 기숙사

    @Enumerated(EnumType.STRING)
    private DormBuilding building;  // 동

    private String floor;       // 층

    private String roomNumber;  // 호수

    private String bedNumber;   // 침대번호

    private String title;      // 제목

    private String officer; // 담당자

    @Lob
    private String content;    // 내용

    @Column(nullable = false)
    private String specificLocation; // 구체적 장소

    @Column(nullable = false)
    private String incidentDate; // 사건 발생 날짜

    @Column(nullable = false)
    private String incidentTime; // 사건 발생 시간

    private boolean isPrivacyAgreed; // 개인정보 동의 여부

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private ComplaintStatus status = ComplaintStatus.PENDING; // 상태 (기본값 대기중)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "complaint", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private ComplaintReply reply;

    @Builder
    public Complaint(ComplaintType type, DormType dormType,
                     String title, String content, User user, String officer, DormBuilding building,
                     String floor,
                     String roomNumber,
                     String bedNumber,
                     String specificLocation,
                     String incidentDate,
                     String incidentTime,
                     boolean isPrivacyAgreed) {
        this.type = type;
        this.dormType = dormType;
        this.title = title;
        this.content = content;
        this.status = ComplaintStatus.PENDING; // 기본 상태
        this.user = user;
        this.officer = officer;
        this.building = building;
        this.floor = floor;
        this.roomNumber = roomNumber;
        this.bedNumber = bedNumber;
        this.specificLocation = specificLocation;
        this.incidentDate = incidentDate;
        this.incidentTime = incidentTime;
        this.isPrivacyAgreed = isPrivacyAgreed;
    }

    // 상태 변경
    public void updateStatus(ComplaintStatus status) {
        this.status = status;
    }

    // 답변 연관관계 설정 (편의 메서드)
    public void addReply(ComplaintReply reply) {
        this.reply = reply;
    }
    
    // 답변 연관관계 해제 (편의 메서드)
    public void removeReply() {
        this.reply = null;
    }

    public void update(RequestComplaintDto dto) {
        this.type = ComplaintType.from(dto.getType());
        this.dormType = DormType.from(dto.getDormType());
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.building = DormBuilding.from(dto.getBuilding());
        this.floor = dto.getFloor();
        this.roomNumber = dto.getRoomNumber();
        this.bedNumber = dto.getBedNumber();
        this.specificLocation = dto.getSpecificLocation();
        this.incidentDate = dto.getIncidentDate();
        this.incidentTime = dto.getIncidentTime();
        this.isPrivacyAgreed = dto.isPrivacyAgreed();
    }

    public void updateOfficer(String officer) {
        this.officer = officer;
    }
}

