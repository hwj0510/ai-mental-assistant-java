package com.heima.aimentalassistant.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JWTTokenProperty {
    private String secret;
    private Long expiration;
    private Long refreshExpiration;
    private String header;
    private String tokenPrefix;
}
