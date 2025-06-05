package com.example.appcenter_project.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

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
    GROUP_ORDER_COMMENT_NOT_OWNED_BY_USER(FORBIDDEN, 3006, "[GroupOrder] 공동구매 게시글의 댓글을 생성한 유저가 아니기 때문에 수정 및 삭제할 권한이 없습니다."),
    GROUP_ORDER_LIKE_NOT_FOUND(NOT_FOUND, 3007, "[GroupOrder] 공동구매 게시글의 좋아요를 누른 유저가 아닙니다."),
    ALREADY_GROUP_ORDER_LIKE_USER(UNAUTHORIZED, 3008, "[GroupOrder] 이미 공동구매 게시글에 좋아요를 누른 유저입니다"),

    // TIP
    TIP_NOT_FOUND(NOT_FOUND, 4001, "[Tip] 팁 게시글을 찾을 수 없습니다."),
    TIP_COMMENT_NOT_FOUND(NOT_FOUND, 4002, "[Tip] 팁 게시글의 댓글을 찾을 수 없습니다."),
    TIP_COMMENT_NOT_OWNED_BY_USER(FORBIDDEN, 4004, "[Tip] 팁 게시글의 댓글을 생성한 유저가 아니기 때문에 수정 및 삭제할 권한이 없습니다."),
    TIP_NOT_OWNED_BY_USER(FORBIDDEN, 4005, "[Tip] 팁 게시글을 생성한 유저가 아니기 때문에 수정 및 삭제할 권한이 없습니다."),
    TIP_LIKE_NOT_FOUND(NOT_FOUND, 4006, "[Tip] 팁 좋아요를 찾을 수 없습니다."),
    ALREADY_TIP_LIKE_USER(NOT_FOUND, 4006, "[Tip] 이미 팁에 좋아요를 누른 유저입니다"),

    // VALIDATION
    VALIDATION_FAILED(BAD_REQUEST, 5001, "[Validation] DTO에서 요청한 값이 올바르지 않습니다."),

    // IMAGE
    DEFAULT_IMAGE_NOT_FOUND(NOT_FOUND, 6002, "[Image] 기본 이미지를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(NOT_FOUND, 6001, "[Image] 이미지를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
