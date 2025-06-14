package com.example.appcenter_project.security.jwt;

import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    public static final long ACCESS_TOKEN_EXPIRED_TIME = 60 * 60 * 1000L;
    public static final long REFRESH_TOKEN_EXPIRED_TIME = 24 * 60 * 60 * 1000L;

    @Value("${jwt.secret}")
    private String secret;

    private final UserRepository userRepository;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(Long id, String studentNumber, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("studentNumber", studentNumber)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRED_TIME))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long id, String studentNumber, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("studentNumber", studentNumber)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRED_TIME))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid Jwt token", e);
        }
    }

    public Authentication getAuthentication(String token) {
        Long userId = Long.valueOf(parseUsername(token));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new JwtException("해당 ID의 사용자가 존재하지 않습니다."));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    public String parseUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build()
                .parseClaimsJws(token).getBody().get("role", String.class);
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
