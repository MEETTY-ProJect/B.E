package com.example.meetty.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtTokenProvider jwtTokenProvider;
    private static final List<String> NO_AUTH_URI = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/verify-email",
            "/api/auth/refresh-token",
            "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars"
    );


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 1. 인증 제외 경로는 필터 건너뛰기
        if (NO_AUTH_URI.stream().anyMatch(requestURI::startsWith)) {
            log.debug("⛔ 인증 제외 경로: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Request Header에서 토큰을 꺼낸다.
        String token = resolveToken(request);


        // 3. validateToken으로 토큰 유효성 검사한 후, 정상적인 토큰이면 Authentication을 가져와서 SecurityContext에 저장한다.
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("✅ 인증 완료: {}", authentication.getName());
        } else {
            log.debug("❌ 유효한 토큰 없음 또는 인증 건너뜀");
        }

        filterChain.doFilter(request, response);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(bearerToken)) {
            log.warn("Authorization 헤더가 없음");
            return null;
        }

        if (!bearerToken.toLowerCase().startsWith("bearer ")) {
            log.warn("Bearer 타입이 아님: {}", bearerToken);
            return null;
        }

        String token = bearerToken.substring(7);
        log.debug("📦 추출된 토큰: {}", token);
        return token;
    }
}
