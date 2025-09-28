package com.example.appcenter_project.config;

import com.example.appcenter_project.security.jwt.JwtFilter;
import com.example.appcenter_project.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        /** 스웨거 **/
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll()

                        /** 이미지 **/
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/static/**").permitAll()

                        /** 파일 **/
                        .requestMatchers("/files/**").permitAll()

                        /** 유저 **/
                        // 로그인
                        .requestMatchers(POST, "/users", "/users/refreshToken").permitAll()
                        // 사용자 권한 수정 및 조회
                        .requestMatchers("/users/role")
                            .hasAnyRole("DORM_LIFE_MANAGER", "DORM_ROOMMATE_MANAGER", "DORM_MANAGER", "ADMIN")
                        // 사용자 정보 조회 및 수정
                        .requestMatchers("/users", "/users/image", "/users/time-table-image").authenticated()
                        // 사용자 게시글 조회(좋아요, 자신의 게시글
                        .requestMatchers("/users/board", "/users/like").authenticated()

                        /** 공지사항 **/
                        // 공지사항 조회
                        .requestMatchers(GET, "/announcements/**").permitAll()
                        // 공지사항 등록, 수정, 삭제
                        .requestMatchers("/announcements/**")
                            .hasAnyRole("DORM_LIFE_MANAGER", "DORM_ROOMMATE_MANAGER", "DORM_MANAGER", "ADMIN")

                        /** 공동구매 **/
                        // 검색 기록 조회
                        .requestMatchers(GET, "/group-orders/searchLog").authenticated()
                        // 공동구매 조회(목록, 단건)
                        .requestMatchers(GET, "/group-orders/**").permitAll()
                        // 공동구매 등록, 수정, 삭제, 좋아요, 완료 처리
                        .requestMatchers("/group-orders/**").authenticated()
                        // 댓글 등록, 삭제
                        .requestMatchers("/group-order-comments/**").authenticated()

                        /** 룸메이트 **/
                        // 나와 유사한 룸메이트 조회
                        .requestMatchers(GET, "/roommates/similar").authenticated()
                        // 나의 체크리스트 조회
                        .requestMatchers(GET, "/roommates/my-checklist").authenticated()
                        // 게시글 좋아요 여부 확인
                        .requestMatchers(GET, "/roommates/{boardId}/liked").authenticated()
                        // 룸메이트 게시글 조회
                        .requestMatchers(GET, "/roommates/**").permitAll()
                        // 룸메이트 게시글 등록, 수정, 삭제
                        .requestMatchers("/roommates/**").authenticated()
                        // 나의 룸메이트
                        .requestMatchers("/my-roommate/**").authenticated()
                        // 룸메이트 매칭
                        .requestMatchers("/roommate-matching/**").authenticated()
                        // 룸메이트 채팅방
                        .requestMatchers("/roommate-chatting-room/**").authenticated()
                        // 룸메이트 채팅
                        .requestMatchers("/roommate/chat/**").authenticated()

                        /** 채팅 **/
                        .requestMatchers("/ws-stomp", "/health").permitAll()

                        /** 팁 **/
                        // 팁 조회
                        .requestMatchers(GET, "/tips/**").permitAll()
                        // 팁 좋아요
                        .requestMatchers(PATCH, "/tips/**").authenticated()
                        // 팁 등록, 수정, 삭제
                        .requestMatchers("/tips/**")
                            .hasAnyRole("DORM_SUPPORTERS", "ADMIN")
                        // 팁 댓글 등록, 삭제
                        .requestMatchers("/tip-comments/**").authenticated()

                        /** 민원 **/
                        // 일반 사용자
                        .requestMatchers("/complaints/**").authenticated()
                        // 관리자, 기숙사 담당자
                        .requestMatchers("/admin/complaints/**")
                            .hasAnyRole("DORM_LIFE_MANAGER", "DORM_ROOMMATE_MANAGER", "DORM_MANAGER", "ADMIN")

                        /** 캘린더 **/
                        // 캘린더 조회
                        .requestMatchers(GET, "/calenders/**").permitAll()
                        // 캘린더 등록, 수정, 삭제
                        .requestMatchers("/calenders/**")
                            .hasAnyRole("DORM_SUPPORTERS", "ADMIN")

                        /** 알림 **/
                        // 알림 조회(로그인한 사용자)
                        .requestMatchers(GET, "/notifications/**").permitAll()
                        // 알림 등록, 수정, 삭제(관리자)
                        .requestMatchers("/notifications/**").hasRole("ADMIN")

                        /** 팝업 알림 **/
                        // 팝업 알림 조회
                        .requestMatchers(GET, "/popup-notifications/**").permitAll()
                        // 팝업 알림 등록, 수정, 삭제(관리자)
                        .requestMatchers("/popup-notifications/**").hasRole("ADMIN")

                        /** 사용자 알림 **/
                        .requestMatchers("/user-notifications/**").authenticated()

                        /** FCM 토큰 **/
                        .requestMatchers("/fcm/token/**").permitAll()

                        /** 나머지 **/
                        .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

        return http.build();
    }
}