package com.example.appcenter_project.domain.studentIdDisclosure.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.studentIdDisclosure.enums.DisclosureRequestStatus;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentIdDisclosureRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisclosureRequestStatus status;

    public static StudentIdDisclosureRequest create(Long requesterId, Long targetId, Long roomId) {
        StudentIdDisclosureRequest request = new StudentIdDisclosureRequest();
        request.requesterId = requesterId;
        request.targetId = targetId;
        request.roomId = roomId;
        request.status = DisclosureRequestStatus.PENDING;
        return request;
    }

    public void accept() {
        if (this.status != DisclosureRequestStatus.PENDING) {
            throw new CustomException(ErrorCode.DISCLOSURE_INVALID_STATUS);
        }
        this.status = DisclosureRequestStatus.ACCEPTED;
    }

    public void reject() {
        if (this.status != DisclosureRequestStatus.PENDING) {
            throw new CustomException(ErrorCode.DISCLOSURE_INVALID_STATUS);
        }
        this.status = DisclosureRequestStatus.REJECTED;
    }

    public void cancel() {
        if (this.status != DisclosureRequestStatus.PENDING) {
            throw new CustomException(ErrorCode.DISCLOSURE_INVALID_STATUS);
        }
        this.status = DisclosureRequestStatus.CANCELED;
    }
}
