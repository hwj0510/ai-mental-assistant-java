package com.heima.aimentalassistant.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AIChatDTO {

    @Size( max = 200, message = "会话标题不能超过200个字符")
    private String sessionTitle;

    @NotBlank(message = "初始消息不能为空")
    @Size( max = 2000, message = "初始消息不能超过2000个字符")
    private String initialMessage;
}
