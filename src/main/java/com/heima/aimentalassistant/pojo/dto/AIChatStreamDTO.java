package com.heima.aimentalassistant.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AIChatStreamDTO {
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "初始消息不能为空")
    @Size( max = 2000, message = "初始消息不能超过2000个字符")
    private String userMessage;
}
