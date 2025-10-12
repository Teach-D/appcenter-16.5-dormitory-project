package com.example.appcenter_project.service.fcm;

import com.example.appcenter_project.entity.user.FcmToken;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.user.FcmTokenRepository;
import com.example.appcenter_project.repository.user.UserRepository;
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
}
