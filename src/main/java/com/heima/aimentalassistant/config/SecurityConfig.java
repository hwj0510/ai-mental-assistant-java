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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final String[] PUBLIC_PATH = {
            "/",
            "/api/user/login",
            "/api/user/add",
            "/api/knowledge/category/tree",
            "/api/file/upload"
    };
    /** 管理员接口路径：任何匹配此模式的请求必须是管理员角色 */
    private static final String[] ADMIN_PATH = {
            "/api/knowledge/article/page",
            "/api/knowledge/article/{id}/status",
            "/api/knowledge/article/{id}",     // PUT/DELETE 是管理员，GET 也要走角色校验
            "/api/emotion-diary/admin/**",
            "/api/psychological-chat/admin/**",
            "/api/data-analytics/**"
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
                        // 1. 公开路径：无需任何认证
                        .requestMatchers(PUBLIC_PATH).permitAll()
                        // 2. 管理员接口：必须是 ADMIN 角色（roleType=2）
                        //    hasRole 会自动加 ROLE_ 前缀，所以 JWT 里的 roleType 映射为 Authority
                        .requestMatchers(ADMIN_PATH).hasRole("ADMIN")
                        // 3. SSE 流式端点：JwtFilter 自己处理（token 可选，Filter 里判断角色）
                        .requestMatchers("/api/psychological-chat/**").permitAll()
                        // 4. 其他已登录用户接口：认证即可
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}