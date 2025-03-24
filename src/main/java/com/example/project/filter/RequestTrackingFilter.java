package com.example.project.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTrackingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestTrackingFilter.class);
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        logger.info("[{}] 시작: {} {}", requestId, request.getMethod(), request.getRequestURI());
        logger.info("[{}] Content-Type: {}", requestId, request.getContentType());

        try {
            chain.doFilter(req, res);
            logger.info("[{}] 완료: {} - 상태 코드: {}", requestId, request.getRequestURI(), response.getStatus());
        } catch (Exception e) {
            logger.error("[{}] 오류: {} - {}", requestId, request.getRequestURI(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return false;
    }
}