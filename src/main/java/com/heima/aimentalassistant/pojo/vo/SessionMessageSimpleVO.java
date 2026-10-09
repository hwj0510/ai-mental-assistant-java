package com.heima.aimentalassistant.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话消息详情（简化版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionMessageSimpleVO {
    /** 消息ID */
    private Long id;
    /** 发送者类型 1-用户 2-AI */
    private Integer senderType;
    /** 消息内容 */
    private String content;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
