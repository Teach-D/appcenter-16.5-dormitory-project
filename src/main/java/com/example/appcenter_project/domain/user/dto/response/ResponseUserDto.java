package com.example.appcenter_project.domain.user.dto.response;

import com.example.appcenter_project.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseUserDto {

    private Long id;
    private String name;
    private String studentNumber;
    private String dormType;
    private String college;
    private int penalty;
    private boolean isRoommateCheckList = false;
    private boolean hasTimeTableImage = false;

    // 이용약관 동의 여부, 초기값 false
    private boolean isTermsAgreed = false;
    // 개인정보처리방침 동의 여부, 초기값 false
    private boolean isPrivacyAgreed = false;
    // 읽지 않은 알림이 있는지
    private boolean hasUnreadNotifications = false;

    public static ResponseUserDto entityToDto(User user, boolean hasUnreadNotifications) {
        return ResponseUserDto.builder()
                .id(user.getId())
                .name(user.getName() != null ? user.getName() : "")
                .studentNumber(user.getStudentNumber())
                .dormType(user.getDormType() != null ? user.getDormType().toValue() : "")
                .college(user.getCollege() != null ? user.getCollege().toValue() : "")
                .penalty(user.getPenalty())
                .hasTimeTableImage(user.getTimeTableImage() != null)
                .isTermsAgreed(user.isTermsAgreed())
                .isPrivacyAgreed(user.isPrivacyAgreed())
                .hasUnreadNotifications(hasUnreadNotifications)
                .build();
    }

    public static ResponseUserDto entityToDto(User user, boolean checkList, boolean hasUnreadNotifications) {
        return ResponseUserDto.builder()
                .id(user.getId())
                .name(user.getName() != null ? user.getName() : "")
                .studentNumber(user.getStudentNumber())
                .dormType(user.getDormType() != null ? user.getDormType().toValue() : "")
                .college(user.getCollege() != null ? user.getCollege().toValue() : "")
                .penalty(user.getPenalty())
                .isRoommateCheckList(checkList)
                .hasTimeTableImage(user.getTimeTableImage() != null)
                .isTermsAgreed(user.isTermsAgreed())
                .isPrivacyAgreed(user.isPrivacyAgreed())
                .hasUnreadNotifications(hasUnreadNotifications)
                .build();
    }

    public static ResponseUserDto entityToDtoNull(User user) {
        return ResponseUserDto.builder()
                .id(user.getId())
                .studentNumber(user.getStudentNumber())
                .build();
    }
}
