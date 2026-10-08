package com.heima.aimentalassistant.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.heima.aimentalassistant.common.properties.JWTTokenProperty;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.Date;

@Component
public class JWTUtils implements ApplicationContextAware {

    private static final String ISSUER = "AI-Mental-Assistant";
    private static ApplicationContext applicationContext;

    //用于在静态方法中获取Bean
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        JWTUtils.applicationContext = applicationContext;
    }
    private static JWTTokenProperty getProperty(){
        return applicationContext.getBean(JWTTokenProperty.class);
    }

    //生成JWT

    public static String generateJWT(String username,Long userId,Integer roleType){
        try{
        //获取JWTTokenProperty
        JWTTokenProperty jwtTokenProperty = getProperty();
        //生成签名的算法
        Algorithm algorithm = Algorithm.HMAC256(jwtTokenProperty.getSecret());
        //生成过期时间
        Date expiration = new Date(System.currentTimeMillis() + jwtTokenProperty.getExpiration());

        //生成JWT
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("roleType", roleType)
                .withExpiresAt(expiration)
                .withIssuedAt(new Date())
                .withIssuer(ISSUER)
                .sign(algorithm);
        }catch (Exception e){
            throw new RuntimeException("生成JWT失败", e);
        }
    }

    //提取Token
    public static String getToken(HttpServletRequest request){
        if(request == null){
            return null;
        }
        String token = request.getHeader("token");
        if(StringUtils.hasText(token)){
            return token;
        }
        return null;
    }
    //从请求属性中获取token
    public static String getCurrentToken(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes != null){
             String token =  (String) requestAttributes.getRequest().getAttribute("jwtToken");
             if(token != null){
                 return token;
             }

             //从请求头中获取token
             token = getToken(requestAttributes.getRequest());
             if(StringUtils.hasText(token)){
                 return token;
             }
        }
        return null;
    }

    //验证token
    public static  DecodedJWT validateToken(String token){
        if (!StringUtils.hasText(token)) {
            throw new JWTVerificationException("token不能为空");
        }
        //token解码
        JWTTokenProperty jwtTokenProperty = getProperty();
        Algorithm algorithm = Algorithm.HMAC256(jwtTokenProperty.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }

    //验证token有效性
    public static TokenValidator verifyToken(String token){
        DecodedJWT decodedJWT = validateToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        String username = decodedJWT.getClaim("username").asString();
        Integer roleType = null;
        try{
            roleType = decodedJWT.getClaim("roleType").asInt();
        }catch (Exception e){
            String roleTypeStr = decodedJWT.getClaim("roleType").asString();
            if (StringUtils.hasText(roleTypeStr)) {
                roleType = Integer.valueOf(roleTypeStr);
            }
        }
        if(userId != null && StringUtils.hasText(username)){
            return new TokenValidator(userId, username, roleType, true);
        }

        return new TokenValidator(userId, username, roleType, false);
    }
    //验证token结果封装类
    @Getter
    public static class TokenValidator{
        private final Long userId;
        private final String username;
        private final Integer roleType;
        private final boolean valid;

        public TokenValidator(Long userId, String username, Integer roleType, Boolean valid) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.valid = valid;
        }
    }

}