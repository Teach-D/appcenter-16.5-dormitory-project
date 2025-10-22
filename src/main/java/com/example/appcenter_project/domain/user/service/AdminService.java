package com.example.appcenter_project.domain.user.service;

import com.example.appcenter_project.domain.user.dto.request.RequestAdminDto;
import com.example.appcenter_project.domain.user.dto.response.ResponseLoginDto;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.appcenter_project.global.exception.ErrorCode.USER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;


    public ResponseLoginDto login(RequestAdminDto requestAdminDto) {
        log.info("[관리자 로그인 시도] loginId: {}", requestAdminDto);

        User admin = userRepository.findByStudentNumber(requestAdminDto.getStudentNumber())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (!admin.getPassword().equals(requestAdminDto.getPassword())) {
            throw new CustomException(USER_NOT_FOUND);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getStudentNumber(), String.valueOf(admin.getRole()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(admin.getId(), admin.getStudentNumber(), String.valueOf(admin.getRole()));
        admin.updateRefreshToken(refreshToken);

        return new ResponseLoginDto(accessToken, refreshToken, admin.getRole().toString());
    }
}
