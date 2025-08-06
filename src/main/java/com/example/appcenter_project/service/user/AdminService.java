package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.request.user.RequestAdminDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.appcenter_project.exception.ErrorCode.USER_NOT_FOUND;

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

        return new ResponseLoginDto(accessToken, refreshToken);
    }
}
