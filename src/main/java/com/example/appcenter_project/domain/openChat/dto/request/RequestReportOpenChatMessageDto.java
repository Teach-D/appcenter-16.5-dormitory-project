package com.example.appcenter_project.domain.openChat.dto.request;

import com.example.appcenter_project.domain.openChat.enums.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RequestReportOpenChatMessageDto {

    @Schema(description = "신고 사유", example = "SPAM_AD",
            allowableValues = {"SPAM_AD", "ABUSE", "FRAUD", "DISRUPTION", "ETC"})
    @NotNull(message = "신고 사유는 필수입니다.")
    private ReportReason reason;
}