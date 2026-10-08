package com.heima.aimentalassistant.config;

import cn.hutool.core.text.AntPathMatcher;
import com.heima.aimentalassistant.common.utils.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final String[] PUBLIC_PATH = {
           "/" ,
            "/api/user/login",
            "/api/test",
            "/api/user/add"
    };

    public static Boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATH) {
            if (antPathMatcher.match(publicPath, path)) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 公开路径无需校验
                        .requestMatchers(PUBLIC_PATH).permitAll()
                        // ⚠️ SSE 流式端点也从 AuthorizationFilter 放行
                        // 认证完全交给 JwtAuthenticationFilter（它在 AuthorizationFilter 之前执行）
                        // 否则 Tomcat async dispatch 会重新走 Filter 链 → SecurityContext 空 → Access Denied
                        .requestMatchers("/api/psychological-chat/**").permitAll()
                        // 其他路径需要校验
                        .anyRequest().authenticated()
                )
                // JwtAuthenticationFilter 仍然会对 /api/psychological-chat/** 严格认证
                // 因为 isPublicPath() 只对 PUBLIC_PATH 返回 true，chat 路径不在里面
                // 如果 token 无效 → Filter 里直接写 response + return，根本到不了 Controller
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}