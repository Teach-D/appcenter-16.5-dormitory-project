package com.example.appcenter_project.global.config;

import com.example.appcenter_project.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand()) || 
            StompCommand.SEND.equals(accessor.getCommand()) ||
            StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            
            String authToken = accessor.getFirstNativeHeader("Authorization");
            if (authToken != null && authToken.startsWith("Bearer ")) {
                try {
                    String token = authToken.substring(7);
                    if (jwtTokenProvider.validateToken(token)) {
                        String userId = jwtTokenProvider.getUserId(token);
                        // 세션에 사용자 정보 저장
                        accessor.getSessionAttributes().put("userId", userId);
                        log.debug("WebSocket authentication successful for userId: {}", userId);
                    }
                } catch (Exception e) {
                    log.warn("WebSocket authentication failed: {}", e.getMessage());
                }
            }
        }
        
        return message;
    }
}
