package com.example.appcenter_project.controller.notification;

import com.example.appcenter_project.dto.request.notification.RequestPopupNotificationDto;
import com.example.appcenter_project.dto.response.notification.ResponsePopupNotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Popup Notification API", description = "팝업 알림 관련 API")
public interface PopupNotificationApiSpecification {

    @Operation(
            summary = "팝업 알림 생성",
            description = "새로운 팝업 알림을 생성합니다. " +
                    "팝업 알림은 앱 실행 시 사용자에게 팝업 형태로 표시됩니다. " +
                    "이미지는 선택사항이며, 여러 개의 이미지를 첨부할 수 있습니다." +
                    "notificationType(룸메이트, 공동구매, 생활원, 유니돔, 서포터즈 중 하나 선택)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "팝업 알림 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다.")
            }
    )
    ResponseEntity<Void> savePopupNotification(
            @RequestPart
            @Parameter(description = "팝업 알림 생성 정보 (title, content, startDate, endDate 등)", required = true)
            RequestPopupNotificationDto requestPopupNotificationDto,
            @RequestPart(value = "images", required = false)
            @Parameter(description = "팝업 알림 이미지 파일 목록 (선택사항)", required = false)
            List<MultipartFile> images);

    @Operation(
            summary = "특정 팝업 알림 조회",
            description = "팝업 알림 ID로 특정 팝업 알림의 상세 정보를 조회합니다. " +
                    "이미지 URL을 포함한 전체 정보를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팝업 알림 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponsePopupNotificationDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팝업 알림입니다.")
            }
    )
    ResponseEntity<ResponsePopupNotificationDto> findPopupNotification(
            @PathVariable
            @Parameter(description = "팝업 알림 ID", required = true, example = "1") Long popupNotificationId,
            HttpServletRequest request);

    @Operation(
            summary = "모든 팝업 알림 조회",
            description = "등록된 모든 팝업 알림을 조회합니다. " +
                    "각 팝업 알림의 이미지 URL을 포함한 전체 정보를 리스트로 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팝업 알림 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponsePopupNotificationDto.class))
                            )
                    )
            }
    )
    ResponseEntity<List<ResponsePopupNotificationDto>> findAllPopupNotifications(
            HttpServletRequest request);

    @Operation(
            summary = "팝업 알림 수정",
            description = "팝업 알림 ID로 특정 팝업 알림의 내용을 수정합니다. " +
                    "이미지는 선택사항이며, 이미지를 제공하면 기존 이미지가 모두 삭제되고 새로운 이미지로 교체됩니다. " +
                    "이미지를 제공하지 않으면 기존 이미지는 유지됩니다.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "팝업 알림 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팝업 알림입니다.")
            }
    )
    ResponseEntity<Void> updatePopupNotification(
            @PathVariable
            @Parameter(description = "팝업 알림 ID", required = true, example = "1") Long popupNotificationId,
            @RequestPart
            @Parameter(description = "수정할 팝업 알림 정보", required = true)
            RequestPopupNotificationDto requestPopupNotificationDto,
            @RequestPart(value = "images", required = false)
            @Parameter(description = "수정할 이미지 파일 목록 (선택사항, 제공시 기존 이미지 완전 교체)", required = false)
            List<MultipartFile> images);

    @Operation(
            summary = "팝업 알림 삭제",
            description = "팝업 알림 ID로 특정 팝업 알림을 삭제합니다. " +
                    "삭제 시 연결된 모든 이미지 파일도 함께 삭제됩니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "팝업 알림 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팝업 알림입니다.")
            }
    )
    ResponseEntity<Void> deletePopupNotification(
            @PathVariable
            @Parameter(description = "팝업 알림 ID", required = true, example = "1") Long popupNotificationId);
}
