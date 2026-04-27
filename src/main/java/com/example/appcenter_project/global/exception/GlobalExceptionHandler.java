package com.example.appcenter_project.global.exception;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.naming.AuthenticationException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final SlackErrorNotifier slackErrorNotifier;

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException ex) {
        log.warn("CustomException 발생: {}", ex.getErrorCode().getMessage());
        return ErrorResponseEntity.toResponseEntity(ex.getErrorCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseEntity> handleAuthenticationException(AuthenticationException ex) {
        log.warn("AuthenticationException 발생: {}", ex.getMessage());
        return ErrorResponseEntity.toResponseEntity(ErrorCode.JWT_ENTRY_POINT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseEntity> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        log.warn("MethodArgumentNotValidException 발생(DTO): {}", errors);
        return ErrorResponseEntity.toResponseEntity(ErrorCode.VALIDATION_FAILED, "DTO에서 요청한 값이 올바르지 않습니다.", errors);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponseEntity> handleMissingPart(MissingServletRequestPartException ex) {
        log.warn("MissingServletRequestPartException 발생: {}", ex.getMessage());

        return ErrorResponseEntity.toResponseEntity(ErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseEntity> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("처리되지 않은 예외 발생: {} {}", request.getMethod(), request.getRequestURI(), ex);
        Sentry.captureException(ex);
        slackErrorNotifier.sendErrorAlert(ex, request);
        return ErrorResponseEntity.toResponseEntity(ErrorCode.UNHANDLED_EXCEPTION);
    }
}