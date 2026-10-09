package com.heima.aimentalassistant.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理端情绪日记 VO
 * 列表接口一次返回全部字段，详情弹窗直接用行数据渲染
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEmotionDiaryVO {

    /** 日志记录ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名（JOIN user 表） */
    private String username;

    /** 昵称（JOIN user 表） */
    private String nickname;

    /** 记录日期 YYYY-MM-DD */
    private LocalDate diaryDate;

    /** 情绪评分 1-10 */
    private Integer moodScore;

    /** 主导情绪 */
    private String dominantEmotion;

    /** 睡眠质量 1-5 */
    private Integer sleepQuality;

    /** 压力水平 1-5 */
    private Integer stressLevel;

    /** 情绪触发因素 */
    private String emotionTriggers;

    /** 日记内容 */
    private String diaryContent;

    /** AI 情绪分析结果（JSON 字符串，前端做 JSON.parse） */
    private String aiEmotionAnalysis;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
