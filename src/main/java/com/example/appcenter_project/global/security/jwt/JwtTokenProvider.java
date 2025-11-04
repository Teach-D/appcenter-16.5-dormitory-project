package com.example.appcenter_project.global.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final long ACCESS_TOKEN_EXPIRED_TIME = 7 * 60 * 60 * 1000L;   // 1시간
    public static final long REFRESH_TOKEN_EXPIRED_TIME = 7 * 24 * 60 * 60 * 1000L; // 1일

    private final UserDetailsService userDetailsService;
    private final Key key;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            UserDetailsService userDetailsService
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.userDetailsService = userDetailsService;
    }

    // Access Token 생성
    public String generateAccessToken(Long id, String studentNumber, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(id))         // sub: id (PK)
                .claim("studentNumber", studentNumber)              // 부가 정보
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRED_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long id, String studentNumber, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(id))         // sub: id (PK)
                .claim("studentNumber", studentNumber)              // 부가 정보
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRED_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        if(token == null) {
            throw new JwtException("Jwt AccessToken not found");
        }
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid Jwt token");
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // JWT에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        String username = parseUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    public String parseUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getStudentNumber(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("studentNumber", String.class);
    }

    // JWT에서 사용자 ID 추출 메서드 추가
    public String getUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // subject가 userId로 설정되어 있음
    }

}