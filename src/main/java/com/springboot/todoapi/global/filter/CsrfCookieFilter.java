package com.springboot.todoapi.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security 6의 DeferredCsrfToken 방식에서는
 * CSRF 쿠키가 자동으로 응답에 실리지 않습니다.
 * 이 필터가 모든 요청마다 토큰을 강제로 로드하여 쿠키를 응답에 커밋합니다.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken(); // 토큰 로드 강제 → 쿠키 응답에 커밋
        }
        filterChain.doFilter(request, response);
    }
}
