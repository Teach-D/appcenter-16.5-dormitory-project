package com.example.appcenter_project.controller.user;

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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "User Notification API", description = "사용자 알림 설정 관련 API")
public interface UserNotificationApiSpecification {

    @Operation(
            summary = "알림 수신 설정 추가",
            description = "사용자의 알림 수신 설정을 추가합니다. " +
                    "알림 타입별로 수신 여부를 설정할 수 있습니다. " +
                    "설정한 타입의 알림만 푸시 알림으로 수신됩니다. " +
                    "알림 타입: 룸메이트, 공동구매, 생활원, 유니돔, 서포터즈",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 수신 설정 추가 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 알림 타입입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<Void> addReceiveNotificationType(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam
            @Parameter(
                    description = "수신할 알림 타입 목록",
                    required = true,
                    example = "[\"공동구매\", \"룸메이트\"]",
                    array = @ArraySchema(schema = @Schema(
                            type = "string",
                            allowableValues = {"룸메이트", "공동구매", "생활원", "유니돔", "서포터즈"}
                    ))
            ) List<String> notificationTypes);

    @Operation(
            summary = "공동구매 키워드 알림 추가",
            description = "공동구매 게시글에 대한 키워드 알림을 추가합니다. " +
                    "키워드가 포함된 제목이나 내용의 공동구매 게시글이 등록되면 푸시 알림을 받습니다. " +
                    "중복된 키워드는 등록할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "키워드 알림 추가 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다."),
                    @ApiResponse(responseCode = "409", description = "이미 등록된 키워드입니다.")
            }
    )
    ResponseEntity<Void> addGroupOrderKeyword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam
            @Parameter(description = "알림받을 키워드", required = true, example = "치킨") String keyword);

    @Operation(
            summary = "공동구매 카테고리 알림 추가",
            description = "공동구매 게시글에 대한 카테고리 알림을 추가합니다. " +
                    "선택한 카테고리의 공동구매 게시글이 등록되면 푸시 알림을 받습니다. " +
                    "중복된 카테고리는 등록할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "카테고리 알림 추가 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 카테고리입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다."),
                    @ApiResponse(responseCode = "409", description = "이미 등록된 카테고리입니다.")
            }
    )
    ResponseEntity<Void> addGroupOrderCategory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam
            @Parameter(
                    description = "알림받을 카테고리",
                    required = true,
                    example = "배달",
                    schema = @Schema(allowableValues = {"전체", "배달", "식자재", "생활용품", "기타"})
            ) String category);

    @Operation(
            summary = "공동구매 키워드 알림 목록 조회",
            description = "현재 사용자가 등록한 공동구매 키워드 알림 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "키워드 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(type = "string", example = "치킨"))
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<List<String>> findUserGroupOrderKeyword(
            @AuthenticationPrincipal CustomUserDetails user);

    @Operation(
            summary = "공동구매 카테고리 알림 목록 조회",
            description = "현재 사용자가 등록한 공동구매 카테고리 알림 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(type = "string", example = "배달"))
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<List<String>> findUserGroupOrderCategory(
            @AuthenticationPrincipal CustomUserDetails user);

    @Operation(
            summary = "공동구매 키워드 알림 수정",
            description = "등록된 공동구매 키워드를 수정합니다. " +
                    "수정 후 키워드가 중복되지 않아야 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "키워드 수정 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 키워드입니다."),
                    @ApiResponse(responseCode = "409", description = "수정 후 키워드가 이미 존재합니다.")
            }
    )
    ResponseEntity<Void> updateGroupOrderKeyword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("beforeKeyword")
            @Parameter(description = "수정할 기존 키워드", required = true, example = "치킨") String beforeKeyword,
            @RequestParam("afterKeyword")
            @Parameter(description = "새로운 키워드", required = true, example = "피자") String afterKeyword);

    @Operation(
            summary = "공동구매 카테고리 알림 수정",
            description = "등록된 공동구매 카테고리를 수정합니다. " +
                    "수정 후 카테고리가 중복되지 않아야 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 카테고리입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리입니다."),
                    @ApiResponse(responseCode = "409", description = "수정 후 카테고리가 이미 존재합니다.")
            }
    )
    ResponseEntity<Void> updateGroupOrderCategory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("beforeKeyword")
            @Parameter(
                    description = "수정할 기존 카테고리",
                    required = true,
                    example = "배달",
                    schema = @Schema(allowableValues = {"전체", "배달", "식자재", "생활용품", "기타"})
            ) String beforeCategory,
            @RequestParam("afterKeyword")
            @Parameter(
                    description = "새로운 카테고리",
                    required = true,
                    example = "식자재",
                    schema = @Schema(allowableValues = {"전체", "배달", "식자재", "생활용품", "기타"})
            ) String afterCategory);

    @Operation(
            summary = "알림 수신 설정 삭제",
            description = "사용자의 알림 수신 설정을 삭제합니다. " +
                    "삭제된 타입의 알림은 더 이상 푸시 알림으로 수신되지 않습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "알림 수신 설정 삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 알림 타입입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<Void> deleteReceiveNotificationType(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam
            @Parameter(
                    description = "삭제할 알림 타입 목록",
                    required = true,
                    example = "[\"공동구매\", \"룸메이트\"]",
                    array = @ArraySchema(schema = @Schema(
                            type = "string",
                            allowableValues = {"룸메이트", "공동구매", "생활원", "유니돔", "서포터즈"}
                    ))
            ) List<String> notificationTypes);

    @Operation(
            summary = "공동구매 키워드 알림 삭제",
            description = "등록된 공동구매 키워드 알림을 삭제합니다. " +
                    "삭제된 키워드는 더 이상 알림을 받지 않습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "키워드 알림 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 키워드입니다.")
            }
    )
    ResponseEntity<Void> deleteUserGroupOrderKeyword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam
            @Parameter(description = "삭제할 키워드", required = true, example = "치킨") String keyword);

    @Operation(
            summary = "공동구매 카테고리 알림 삭제",
            description = "등록된 공동구매 카테고리 알림을 삭제합니다. " +
                    "삭제된 카테고리는 더 이상 알림을 받지 않습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "카테고리 알림 삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 카테고리입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리입니다.")
            }
    )
    ResponseEntity<Void> deleteUserGroupOrderCategory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam
            @Parameter(
                    description = "삭제할 카테고리",
                    required = true,
                    example = "배달",
                    schema = @Schema(allowableValues = {"전체", "배달", "식자재", "생활용품", "기타"})
            ) String category);

    @Operation(
            summary = "개별 사용자 알림 삭제",
            description = "사용자의 알림 목록에서 특정 알림을 삭제합니다. " +
                    "삭제된 알림은 해당 사용자의 알림 목록에서 제거되며, 다른 사용자에게는 영향을 주지 않습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "사용자 알림 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 알림입니다.")
            }
    )
    ResponseEntity<Void> deleteUserNotification(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "삭제할 알림 ID", required = true, example = "1") Long notificationId);
}
