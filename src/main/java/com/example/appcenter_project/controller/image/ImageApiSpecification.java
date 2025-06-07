package com.example.appcenter_project.controller.image;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "Image", description = "기본 이미지 관련 API")
public interface ImageApiSpecification {

    @Operation(
            summary = "유저 기본 등록",
            description = "유저의 기본 이미지를 등록합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "유저 기본 이미지 등록 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "입력이 잘못되었습니다."
                    )
            }
    )
    ResponseEntity<Void> setDefaultUserImage(@RequestPart MultipartFile image);
}
