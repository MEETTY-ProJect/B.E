package com.example.meetty.global.jwt;

import com.example.meetty.global.config.auth.CustomUserDetailsServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.DecodingException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    private final CustomUserDetailsServiceImpl customUserDetailsService;

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.jwt.token.access-expiration-time}")
    private Long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private Long refreshExpirationTime;

    // 토큰 생성
    public String generateToken(String email, Long userId, Long expireTime, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("userId", userId);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    // 액세스 토큰 만들기
    public String createAccessToken(String email, Long userId, String role) {
        return generateToken(email, userId, accessExpirationTime, role);
    }


    // 리프레시 토큰 만들기
    public String createRefreshToken(String email, Long userId, String role) {
        return generateToken(email, userId, refreshExpirationTime, role);
    }


    // 토큰 분해하기
    public String parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }


    // 인증정보 가져오기
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(this.parseClaims(token));

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }


    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT Token.", e);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT Token.", e);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT Token.", e);
        } catch (IllegalArgumentException | DecodingException e) {
            log.warn("JWT 파싱 실패: {}", e.getMessage());
        }

        return false;
    }

    // 토큰에서 이메일 가져오기
    public String getEmailByToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            log.warn("getEmailByToken 실패: {}", e.getMessage());
            return null;
        }
    }

    // 토큰에서 userId 가져오기
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", Long.class);

        } catch (JwtException e) {
            log.warn("getUserIdFromToken 실패: {}", e.getMessage());
            return null;
        }
    }
}
