package com.example.appcenter_project.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseUserAgreementDto {

    private boolean isTermsAgreed;
    private boolean isPrivacyAgreed;

    public static ResponseUserAgreementDto of(boolean isTermsAgreed, boolean isPrivacyAgreed) {
        return new ResponseUserAgreementDto(isTermsAgreed, isPrivacyAgreed);
    }
}
