package com.example.appcenter_project.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // JWT
    JWT_NOT_VALID(UNAUTHORIZED, 1001, "[Jwt] 유효하지 않은 Jwt"),
    JWT_ACCESS_TOKEN_EXPIRED(UNAUTHORIZED, 1002, "[Jwt] 만료된 엑세스 토큰입니다."),
    JWT_REFRESH_TOKEN_EXPIRED(UNAUTHORIZED, 1003, "[Jwt] 만료된 리프레시 토큰입니다."),
    JWT_MALFORMED(UNAUTHORIZED, 1004, "[Jwt] 잘못된 토큰 형식입니다."),
    JWT_SIGNATURE(UNAUTHORIZED, 1005, "[Jwt] 유효하지 않은 서명입니다."),
    JWT_UNSUPPORTED(UNAUTHORIZED, 1006, "[Jwt] 지원하지 않는 토큰입니다."),
    JWT_ENTRY_POINT(UNAUTHORIZED, 1007, "[Jwt] 인증되지 않은 사용자입니다."),
    JWT_ACCESS_DENIED(FORBIDDEN, 1008, "[Jwt] 리소스에 접근할 권한이 없습니다."),

    // USER
    USER_NOT_FOUND(NOT_FOUND, 2001, "[User] 사용자를 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, 2002, "[User] 유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_USER_NOT_FOUND(NOT_FOUND, 2003, "[User] 해당 Refresh Token과 일치하는 사용자가 없습니다."),

    // GROUP_ORDER
    GROUP_ORDER_NOT_FOUND(NOT_FOUND, 3001, "[GroupOrder] 공동구매 글을 찾을 수 없습니다."),
    GROUP_ORDER_CHAT_ROOM_NOT_FOUND(NOT_FOUND, 3002, "[GroupOrder] 채팅방을 찾을 수 없습니다."),
    USER_GROUP_ORDER_NOT_FOUND(NOT_FOUND, 3003, "[GroupOrder] 사용자의 공동구매 참여 정보를 찾을 수 없습니다."),
    GROUP_ORDER_COMMENT_NOT_FOUND(NOT_FOUND, 3004, "[GroupOrder] 댓글을 찾을 수 없습니다."),
    GROUP_ORDER_TITLE_DUPLICATE(CONFLICT, 3005, "[GroupOrder] 이미 존재하는 제목입니다."),
    GROUP_ORDER_NOT_OWNED_BY_USER(FORBIDDEN, 3006, "[GroupOrder] 공동구매 게시글을 생성한 유저가 아니기 때문에 수정 및 삭제할 권한이 없습니다."),
    GROUP_ORDER_COMMENT_NOT_OWNED_BY_USER(FORBIDDEN, 3007, "[GroupOrder] 공동구매 게시글의 댓글을 생성한 유저가 아니기 때문에 수정 및 삭제할 권한이 없습니다."),
    GROUP_ORDER_LIKE_NOT_FOUND(NOT_FOUND, 3008, "[GroupOrder] 공동구매 게시글의 좋아요를 누른 유저가 아닙니다."),
    ALREADY_GROUP_ORDER_LIKE_USER(UNAUTHORIZED, 3009, "[GroupOrder] 이미 공동구매 게시글에 좋아요를 누른 유저입니다"),
    USER_GROUP_ORDER_CHAT_ROOM_NOT_FOUND(NOT_FOUND, 30010, "[GroupOrder] 사용자의 공동구매 채팅방 참여 정보를 찾을 수 없습니다."),

    // TIP
    TIP_NOT_FOUND(NOT_FOUND, 4001, "[Tip] 팁 게시글을 찾을 수 없습니다."),
    TIP_COMMENT_NOT_FOUND(NOT_FOUND, 4002, "[Tip] 팁 게시글의 댓글을 찾을 수 없습니다."),
    TIP_COMMENT_NOT_OWNED_BY_USER(FORBIDDEN, 4004, "[Tip] 팁 게시글의 댓글을 생성한 유저가 아니기 때문에 수정 및 삭제할 권한이 없습니다."),
    TIP_NOT_OWNED_BY_USER(FORBIDDEN, 4005, "[Tip] 팁 게시글을 생성한 유저가 아니기 때문에 수정 및 삭제할 권한이 없습니다."),
    TIP_LIKE_NOT_FOUND(NOT_FOUND, 4006, "[Tip] 팁 좋아요를 찾을 수 없습니다."),
    ALREADY_TIP_LIKE_USER(UNAUTHORIZED, 4006, "[Tip] 이미 팁에 좋아요를 누른 유저입니다"),
    NOT_LIKED_TIP(UNAUTHORIZED, 4006, "[Tip] 팁에 좋아요를 누른 유저가 아닙니다."),


    // VALIDATION
    VALIDATION_FAILED(BAD_REQUEST, 5001, "[Validation] Request에서 요청한 값이 올바르지 않습니다."),

    // IMAGE
    DEFAULT_IMAGE_NOT_FOUND(NOT_FOUND, 6002, "[Image] 기본 이미지를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(NOT_FOUND, 6001, "[Image] 이미지를 찾을 수 없습니다."),

    // ROOMMATE
    // ROOMMATE
    ROOMMATE_USER_NOT_FOUND(NOT_FOUND, 7001, "[Roommate] 해당 유저가 존재하지 않습니다."),
    ROOMMATE_BOARD_NOT_FOUND(NOT_FOUND, 7002, "[Roommate] 게시글을 찾을 수 없습니다."),
    ROOMMATE_CHECKLIST_NOT_FOUND(NOT_FOUND, 7003, "[Roommate] 체크리스트를 찾을 수 없습니다."),
    ROOMMATE_BOARD_ALREADY_EXISTS(CONFLICT, 7004, "[Roommate] 이미 작성된 게시글이 있습니다."),
    ROOMMATE_FORBIDDEN_ACCESS(FORBIDDEN, 7005, "[Roommate] 접근 권한이 없습니다."),
    ROOMMATE_NO_SIMILAR_BOARD(NOT_FOUND, 7006, "[Roommate] 유사도 비교할 게시글이 없습니다."),
    ROOMMATE_UPDATE_NOT_ALLOWED(FORBIDDEN, 7007, "[Roommate] 수정 권한이 없습니다."),
    ROOMMATE_CHECKLIST_UPDATE_FAILED(BAD_REQUEST, 7008, "[Roommate] 체크리스트 수정에 실패했습니다."),

    // ROOMMATE_BOARD_LIKE
    ROOMMATE_BOARD_LIKE_NOT_FOUND(NOT_FOUND, 7501, "[RoommateBoard] 좋아요 정보를 찾을 수 없습니다."),
    ALREADY_ROOMMATE_BOARD_LIKE_USER(UNAUTHORIZED, 7502, "[RoommateBoard] 이미 해당 게시글에 좋아요를 누른 유저입니다."),

    // ROOMMATE_MATCHING
    ROOMMATE_MATCHING_ALREADY_REQUESTED(CONFLICT, 8101, "[RoommateMatching] 이미 해당 사용자에게 매칭 요청을 보냈습니다."),
    ROOMMATE_MATCHING_NOT_FOUND(NOT_FOUND, 8102, "[RoommateMatching] 해당 매칭 요청을 찾을 수 없습니다."),
    ROOMMATE_MATCHING_ALREADY_COMPLETED(BAD_REQUEST, 8103, "[RoommateMatching] 이미 수락되었거나 실패한 요청입니다."),
    ROOMMATE_MATCHING_NOT_FOR_USER(FORBIDDEN, 8104, "[RoommateMatching] 해당 매칭 요청은 현재 사용자와 관련이 없습니다."),
    ROOMMATE_ALREADY_MATCHED(HttpStatus.CONFLICT,8105,"[RoommateMatching] 이미 매칭된 사람이 있습니다."),

    // ROOMMATE_MYROOMMATE
    MY_ROOMMATE_NOT_REGISTERED(NOT_FOUND, 9201, "[MyRoommate] 해당 사용자의 룸메이트 정보를 찾을 수 없습니다"),
    RULE_NOT_FOUND(NOT_FOUND, 9203, "[MyRoommate] 수정할 규칙이 존재하지 않습니다."),

    // ROOMMATE_CHAT
    ROOMMATE_CHAT_CANNOT_CHAT_WITH_SELF(BAD_REQUEST, 10001, "[RoommateChat] 자신에게는 채팅을 보낼 수 없습니다."),
    DUPLICATE_CHAT_ROOM(CONFLICT, 10001, "[RoommateChat] 이미 존재하는 채팅방입니다."),
    ROOMMATE_CHAT_ROOM_NOT_FOUND(NOT_FOUND, 7013, "[RoommateChat] 채팅방을 찾을 수 없습니다."),
    ROOMMATE_CHAT_ROOM_FORBIDDEN(FORBIDDEN, 10004, "[RoommateChat] 이 채팅방에 속하지 않은 사용자입니다."),
    ROOMMATE_CHAT_ROOM_DENIED(PRECONDITION_FAILED, 10004, "[RoommateChat] 양방향 채팅방 생성은 할 수 없습니다."),


    // REPORT
    REPORT_NOT_REGISTERED(NOT_FOUND, 11001, "[Report] 해당 신고 정보를 찾을 수 없습니다"),

    // ANNOUNCEMENT
    ANNOUNCEMENT_NOT_REGISTERED(NOT_FOUND, 12001, "[ANNOUNCEMENT] 해당 공지사항 정보를 찾을 수 없습니다"),

    // ATTACHEDFILE
    ATTACHEDFILE_NOT_REGISTERED(NOT_FOUND, 13001, "[ATTACHEDFILE] 해당 파일을 찾을 수 없습니다"),

    // CALENDER
    CALENDER_NOT_REGISTERED(NOT_FOUND, 13001, "[CALENDER] 해당 캘린더 정보를 찾을 수 없습니다");


    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
