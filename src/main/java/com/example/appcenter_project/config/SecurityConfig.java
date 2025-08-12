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
                        .requestMatchers("/ws-stomp", "/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/users", "/users/refreshToken").permitAll()
                        .requestMatchers(GET, "/tips/**", "/group-orders/**", "/roommates/**").permitAll()
                        // 이미지 관련 엔드포인트 모두 허용
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/static/**").permitAll()
                        .requestMatchers("/admins/login").permitAll()
                        // 공지사항 관련 엔드포인트
                        .requestMatchers(POST, "/announcements/**").hasRole("ADMIN")
                        .requestMatchers(PUT, "/announcements/**").hasRole("ADMIN")
                        .requestMatchers(DELETE, "/announcements/**").hasRole("ADMIN")
                        .requestMatchers(GET, "/announcements/**").permitAll()
                        .requestMatchers("/files/**").permitAll()

                        // 캘린더 관련 엔드포인트
                        .requestMatchers(POST, "/calenders/**").hasRole("ADMIN")
                        .requestMatchers(PUT, "/calenders/**").hasRole("ADMIN")
                        .requestMatchers(DELETE, "/calenders/**").hasRole("ADMIN")
                        .requestMatchers(GET, "/calenders/**").permitAll()

                        // 공동구매관련 엔드포인트
                        .requestMatchers(GET, "/group-orders/**").permitAll()


                        .requestMatchers(GET, "/reports/**").hasRole("ADMIN")
                        .requestMatchers(DELETE, "/reports/**").hasRole("ADMIN")
                        .requestMatchers("/**").hasRole("USER")
                        .anyRequest().authenticated())
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); // JWT 필터 추가

        return http.build();
    }
}