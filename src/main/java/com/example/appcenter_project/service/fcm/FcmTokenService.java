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

        FcmToken build = FcmToken.builder()
                .user(user)
                .token(token).build();

        fcmTokenRepository.save(build);
    }
}
