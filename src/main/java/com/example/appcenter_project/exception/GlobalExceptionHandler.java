package com.example.appcenter_project.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

}