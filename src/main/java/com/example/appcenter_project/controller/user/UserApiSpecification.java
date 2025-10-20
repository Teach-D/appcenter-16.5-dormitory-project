package com.example.appcenter_project.controller.user;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.user.*;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.dto.response.user.ResponseUserDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "User", description = "사용자 관리 API")
public interface UserApiSpecification {

    @Operation(
            summary = "사용자 회원가입 및 로그인",
            description = "학번으로 회원가입을 진행합니다. 이미 등록된 학번이면 바로 로그인하고, 새로운 학번이면 회원가입 후 로그인합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입/로그인 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseLoginDto.class))),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "기본 이미지를 찾을 수 없습니다. (DEFAULT_IMAGE_NOT_FOUND)", content = @Content(examples = {}))
            }
    )
    ResponseEntity<ResponseLoginDto> saveUser(
            @Valid @RequestBody
            @Parameter(description = "회원가입 정보", required = true) SignupUser signupUser);


    @Operation(
            summary = "액세스 토큰 재발급",
            description = "유효한 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"))),
                    @ApiResponse(responseCode = "401",
                            description = "유효하지 않은 Refresh Token입니다. (INVALID_REFRESH_TOKEN)",
                            content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404",
                            description = "해당 Refresh Token과 일치하는 사용자가 없습니다. (REFRESH_TOKEN_USER_NOT_FOUND)",
                            content = @Content(examples = {})
                    )
            }
    )
    ResponseEntity<?> reissueAccessToken(
            @RequestBody
            @Parameter RequestTokenDto requestTokenDto
    );

    @Operation(
            summary = "특정 유저 푸시 알림 전송",
            description = "userId, title, body를 받아서 특정 유저에게 푸시 알림 전송(관리자용)"
    )
    ResponseEntity<Void> sendPushNotification(@RequestBody RequestUserPushNotification requestUserPushNotification);

    @Operation(
            summary = "사용자 권한 변경",
            description = "생활원 직원, 관리자만 특정 사용자의 권한을 생활원 직원으로 변경할 수 있음" +
                    "(role : 기숙사 담당자, 기숙사 생활민원 담당자, 기숙사 룸메이트민원 담당자)"
    )
    ResponseEntity<Void> changeUserRole(@RequestBody RequestUserRoleDto requestUserRoleDto);

    @Operation(
            summary = "사용자 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseUserDto.class))),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다."),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)")
            }
    )
    ResponseEntity<ResponseUserDto> findUser(@AuthenticationPrincipal CustomUserDetails user);


    @Operation(
            summary = "모든 유저의 정보 조회",
            description = "모든 유저의 id, studentNumber를 조회합니다(관리자요)"
    )
    ResponseEntity<List<ResponseUserDto>> findAllUsers();


    @Operation(
        summary = "사용자 프로필 이미지 조회",
        description = "현재 로그인한 사용자의 프로필 이미지를 조회합니다.",
        responses = {
                @ApiResponse(responseCode = "200", description = "이미지 조회 성공",
                        content = @Content(mediaType = "image/*")),
                @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                @ApiResponse(responseCode = "404",
                        description = """
                        다음 중 하나일 수 있습니다:
                        - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                        - 이미지를 찾을 수 없습니다. (IMAGE_NOT_FOUND)
                        """,
                        content = @Content(examples = {})
                )
        }
    )
    ResponseEntity<ImageLinkDto> findUserImage(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request);

    @Operation(
            summary = "사용자가 작성한 게시글 조회",
            description = "현재 로그인한 사용자가 작성한 모든 게시글(팁, 공동구매)을 최신순으로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시글 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseBoardDto.class)))),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)", content = @Content(examples = {}))
            }
    )
    ResponseEntity<List<ResponseBoardDto>> findUserBoards(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request);

    @Operation(
            summary = "사용자가 좋아요한 게시글 조회",
            description = "현재 로그인한 사용자가 좋아요를 누른 모든 게시글(팁, 공동구매)을 최신순으로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요한 게시글 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseBoardDto.class)))),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)", content = @Content(examples = {}))
            }
    )
    ResponseEntity<List<ResponseBoardDto>> findUserLikedBoards(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request);

    @Operation(
            summary = "개인정보, 이용약관 동의 처리"
    )
    ResponseEntity<Void> updateUserAgreement(@AuthenticationPrincipal CustomUserDetails user, @Parameter(description = "이용약관 동의") boolean isTermsAgreed, @Parameter(description = "개인정보처리방침 동의") boolean isPrivacyAgreed);


    @Operation(
            summary = "사용자 정보 수정",
            description = "현재 로그인한 사용자의 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseUserDto.class))),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)", content = @Content(examples = {}))
            }
    )
    ResponseEntity<ResponseUserDto> updateUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody
            @Parameter(description = "수정할 사용자 정보", required = true) RequestUserDto requestUserDto);

    @Operation(
            summary = "사용자 프로필 이미지 수정",
            description = "현재 로그인한 사용자의 프로필 이미지를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "프로필 이미지 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 이미지 파일입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                            - 이미지를 찾을 수 없습니다. (IMAGE_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    )
            }
    )
    ResponseEntity<Void> updateUserImage(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart
            @Parameter(description = "업로드할 이미지 파일", required = true) MultipartFile image);

    @Operation(
            summary = "사용자 시간표 이미지 업로드/수정",
            description = "현재 로그인한 사용자의 시간표 이미지를 업로드하거나 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "시간표 이미지 저장/수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 이미지 파일입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                            - 이미지를 찾을 수 없습니다. (IMAGE_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    )
            }
    )
    ResponseEntity<Void> updateUserTimeTableImage(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart
            @Parameter(description = "업로드할 시간표 이미지 파일", required = true) MultipartFile image);

    @Operation(
            summary = "사용자 시간표 이미지 조회",
            description = "현재 로그인한 사용자의 시간표 이미지를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "시간표 이미지 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ImageLinkDto.class))),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                            - 시간표 이미지를 찾을 수 없습니다. (IMAGE_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    )
            }
    )
    ResponseEntity<ImageLinkDto> findUserTimeTableImage(@AuthenticationPrincipal CustomUserDetails user, HttpServletRequest request);

    @Operation(
            summary = "사용자 시간표 이미지 삭제",
            description = "현재 로그인한 사용자의 시간표 이미지를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "시간표 이미지 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                            - 시간표 이미지를 찾을 수 없습니다. (IMAGE_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    )
            }
    )
    ResponseEntity<Void> deleteUserTimeTableImage(@AuthenticationPrincipal CustomUserDetails user);

    @Operation(
            summary = "사용자 탈퇴",
            description = "현재 로그인한 사용자의 계정을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "사용자 탈퇴 성공"),
                    @ApiResponse(responseCode = "403", description = "인증되지 않은 사용자입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)", content = @Content(examples = {}))
            }
    )
    ResponseEntity<Void> deleteUser(@AuthenticationPrincipal CustomUserDetails user);
}
