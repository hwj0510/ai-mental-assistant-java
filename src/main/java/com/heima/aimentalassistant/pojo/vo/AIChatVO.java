package com.heima.aimentalassistant.pojo.vo;

import lombok.Data;

public class AIChatVO {

    public record StreamChatSession(
            String sessionId,
            Long userHash,
            String initialMessage,
            Long startTime,
            Long expiryTime,
            Integer messageCount,
            String status
    ) {}

}


