package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.entity.FcmToken;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void saveToken(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 등록된 토큰이면 중복 저장 방지
        if (fcmTokenRepository.existsByToken(token)) {
            return;
        }

        // 해당 유저의 기존 토큰 찾기
        fcmTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        existing -> existing.updateToken(token), // 기존 토큰 갱신
                        () -> { // 없으면 새로 생성
                            FcmToken newToken = FcmToken.builder()
                                    .user(user)
                                    .token(token)
                                    .build();
                            fcmTokenRepository.save(newToken);
                            user.addFcmToken(newToken);
                        }
                );
    }

    @Transactional
    public void saveToken(CustomUserDetails userDetails, String token) {

        // 로그인한 경우
        if(!(userDetails == null)) {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // 이미 등록된 토큰이면 중복 저장 방지
            if (fcmTokenRepository.existsByToken(token)) {
                FcmToken fcmToken = fcmTokenRepository.findByToken(token);

                if (fcmToken.getUser() == null ||  fcmToken.getUser() != user){
                    fcmToken.updateUser(user);
                }
                return;
            }

            FcmToken newToken = FcmToken.builder()
                    .user(user)
                    .token(token)
                    .build();

            fcmTokenRepository.save(newToken);

        } else {
            // 이미 등록된 토큰이면 중복 저장 방지
            if (fcmTokenRepository.existsByToken(token)) {
                return;
            }

            FcmToken fcmToken = FcmToken.builder()
                    .token(token)
                    .build();
            fcmTokenRepository.save(fcmToken);
        }

    }
}
