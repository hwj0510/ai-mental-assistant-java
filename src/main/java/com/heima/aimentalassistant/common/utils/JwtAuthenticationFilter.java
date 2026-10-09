package com.heima.aimentalassistant.common.utils;

import com.heima.aimentalassistant.common.enums.ResultCode;
import com.heima.aimentalassistant.config.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return SecurityConfig.isPublicPath(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = JWTUtils.getToken(request);

        if (StringUtils.hasText(token)) {
            JWTUtils.TokenValidator tokenValidator = JWTUtils.verifyToken(token);
            if (tokenValidator != null && tokenValidator.isValid()) {
                // ✅ 验证通过：构造 Spring Security 认证对象
                // roleType 1=普通用户 → ROLE_USER，roleType 2=管理员 → ROLE_ADMIN
                java.util.List<org.springframework.security.core.GrantedAuthority> authorities =
                        new java.util.ArrayList<>();
                if (tokenValidator.getRoleType() != null) {
                    String role = tokenValidator.getRoleType() == 2 ? "ADMIN" : "USER";
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
                }
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                tokenValidator.getUsername(),
                                null,
                                authorities
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                cleanContext();
                ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
                return;  // ⚠️ 加 return，不合法就别往下走了
            }
        } else {
            cleanContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;  // ⚠️ 同样加 return
        }

        filterChain.doFilter(request, response);
    }

    private void cleanContext() {
        SecurityContextHolder.clearContext();
    }
}