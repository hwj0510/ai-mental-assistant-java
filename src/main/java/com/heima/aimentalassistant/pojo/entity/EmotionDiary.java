package com.heima.aimentalassistant.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("emotion_diary")
@Builder
public class EmotionDiary {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("diary_date")
    private LocalDate diaryDate;

    /** 情绪评分 1-10 */
    @TableField("mood_score")
    private Integer moodScore;

    /** 主导情绪：开心 / 平静 / 焦虑 / 悲伤 / 愤怒 */
    @TableField("dominant_emotion")
    private String dominantEmotion;

    /** 情绪触发因素 */
    @TableField("emotion_triggers")
    private String emotionTriggers;

    /** 日记内容 */
    @TableField("diary_content")
    private String diaryContent;

    /** 睡眠质量 1-5 */
    @TableField("sleep_quality")
    private Integer sleepQuality;

    /** 压力水平 1-5 */
    @TableField("stress_level")
    private Integer stressLevel;

    /** AI 情绪分析结果（TEXT 存 JSON 字符串） */
    @TableField("ai_emotion_analysis")
    private String aiEmotionAnalysis;

    @TableField("create_at")
    private LocalDateTime createAt;

    @TableField("update_at")
    private LocalDateTime updateAt;
}