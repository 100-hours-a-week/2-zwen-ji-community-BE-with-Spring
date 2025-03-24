package com.example.project.security;

import com.example.project.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 기존 방식대로 헤더에서 토큰 추출 시도
        String token = getJwtFromRequest(request);

        // 헤더에서 토큰을 찾지 못했고, 요청 경로가 이미지 관련이면 쿼리 파라미터에서 토큰 찾기 시도
        if (!StringUtils.hasText(token) && isImageRequest(request)) {
            token = request.getParameter("token");
        }

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isImageRequest(HttpServletRequest request) {
        // 이미지 요청 경로에 대한 확인 로직
        // 예: /images/ 로 시작하거나 특정 이미지 확장자를 가진 요청인지 확인
        String path = request.getRequestURI();
        return path.startsWith("/images/") ||
                path.startsWith("/static.upload/profiles/") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".png") ||
                path.endsWith(".gif") ||
                path.endsWith(".JPG"); // 대문자 확장자도 포함
    }
}