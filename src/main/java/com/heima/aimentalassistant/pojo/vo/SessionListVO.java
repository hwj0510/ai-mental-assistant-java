package com.heima.aimentalassistant.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话列表项 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionListVO {
    /** 会话ID */
    private Long id;
    /** 用户昵称（管理端列表需要关联用户表） */
    private String userNickname;
    /** 会话标题 */
    private String sessionTitle;
    /** 开始时间 */
    private LocalDateTime startedAt;
    /** 最后一条消息内容 */
    private String lastMessageContent;
    /** 消息总数 */
    private Integer messageCount;
    /** 最后一条消息时间（列表按此字段倒序） */
    private LocalDateTime lastMessageTime;
}