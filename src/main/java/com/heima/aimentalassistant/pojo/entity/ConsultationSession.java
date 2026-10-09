package com.heima.aimentalassistant.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("consultation_session")
@Builder
public class ConsultationSession {
    // 会话ID
    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户ID
    @TableField("user_id")
    private Long userId;

    // 会话标题
    @Size(max = 200, message = "会话标题长度不能超过200个字符")
    @TableField("session_title")
    private String sessionTitle;

    // 开始时间
    @TableField("started_at")
    private LocalDateTime startedAt;

    // 消息总数（冗余字段，新增消息时同步更新）
    @TableField("message_count")
    private Integer messageCount;

    // 最后一条消息内容（冗余字段）
    @TableField("last_message_content")
    private String lastMessageContent;

    // 最后一条消息时间（冗余字段）
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    // 软删除标记 0-正常 1-已删除
    @TableField("deleted")
    private Integer deleted;

    // 最后一次情绪分析结果(JSON格式)
    @TableField("last_emotion_analysis")
    private String lastEmotionAnalysis;

    // 最后一次情绪分析更新时间
    @TableField("last_emotion_updated_at")
    private LocalDateTime lastEmotionUpdatedAt;
}