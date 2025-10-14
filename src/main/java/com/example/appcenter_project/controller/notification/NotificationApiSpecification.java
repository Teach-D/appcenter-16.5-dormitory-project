package com.example.appcenter_project.controller.notification;

import com.example.appcenter_project.dto.request.notification.RequestNotificationDto;
import com.example.appcenter_project.dto.response.notification.ResponseNotificationDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Notification API", description = "알림 관련 API")
public interface NotificationApiSpecification {

    @Operation(
            summary = "알림 생성",
            description = "새로운 알림을 생성하고 대상 사용자들에게 푸시 알림을 전송합니다. " +
                    "알림 타입에 따라 전송 대상이 달라집니다: " +
                    "- 유니돔: 모든 사용자에게 전송 " +
                    "- 생활원: 기숙사 거주 학생(dormType이 NONE이 아닌 학생)에게만 전송 " +
                    "- 기타: 개별 사용자 알림",
            responses = {
                    @ApiResponse(responseCode = "201", description = "알림 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다.")
            }
    )
    ResponseEntity<Void> saveNotification(
            @RequestBody
            @Parameter(description = "알림 생성 정보 (title, body, notificationType, boardId)", required = true)
            RequestNotificationDto requestNotificationDto);

    @Operation(
            summary = "사용자 알림 목록 조회",
            description = "현재 로그인한 사용자의 모든 알림을 최신순으로 조회합니다. " +
                    "조회 시 모든 알림이 자동으로 읽음 처리됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "알림 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseNotificationDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<List<ResponseNotificationDto>> findNotifications(
            @AuthenticationPrincipal CustomUserDetails user);

    @Operation(
            summary = "개인 알림 전송",
            description = "학번을 통해서 개인 알림이 전송됩니다.(푸시 알림 포함)"
    )
    ResponseEntity<Void> saveNotificationByStudentNumber(@RequestBody RequestNotificationDto requestNotificationDto, String studentNumber);


        @Operation(
            summary = "특정 알림 상세 조회",
            description = "알림 ID로 특정 알림의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "알림 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseNotificationDto.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 알림입니다.")
            }
    )
    ResponseEntity<ResponseNotificationDto> findNotification(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("notificationId")
            @Parameter(description = "알림 ID", required = true, example = "1") Long notificationId);

    @Operation(
            summary = "알림 수정",
            description = "알림 ID로 특정 알림의 내용을 수정합니다. " +
                    "제목, 내용, 알림 타입, 연결된 게시글 ID를 수정할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 알림입니다.")
            }
    )
    ResponseEntity<Void> updateNotification(
            @PathVariable
            @Parameter(description = "알림 ID", required = true, example = "1") Long notificationId,
            @RequestBody
            @Parameter(description = "수정할 알림 정보", required = true) RequestNotificationDto requestNotificationDto);

    @Operation(
            summary = "알림 삭제",
            description = "알림 ID로 특정 알림을 삭제합니다. " +
                    "삭제된 알림은 모든 사용자의 알림 목록에서 제거됩니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "알림 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 알림입니다.")
            }
    )
    ResponseEntity<Void> deleteNotification(
            @PathVariable
            @Parameter(description = "알림 ID", required = true, example = "1") Long notificationId);
}
