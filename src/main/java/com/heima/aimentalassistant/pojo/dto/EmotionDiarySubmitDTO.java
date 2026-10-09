package com.heima.aimentalassistant.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmotionDiarySubmitDTO {

    /** 日记日期 */
    @NotNull(message = "日记日期不能为空")
    private LocalDate diaryDate;

    /** 情绪评分 1-10 */
    @NotNull(message = "情绪评分不能为空")
    @Min(value = 1, message = "情绪评分最小为 1")
    @Max(value = 10, message = "情绪评分最大为 10")
    private Integer moodScore;

    /** 主导情绪 */
    @NotBlank(message = "主导情绪不能为空")
    private String dominantEmotion;

    /** 情绪触发因素（可选） */
    private String emotionTriggers;

    /** 日记内容（可选） */
    private String diaryContent;

    /** 睡眠质量 1-5，前端传字符串，Jackson 自动转 Integer */
    @Min(value = 1, message = "睡眠质量最小为 1")
    @Max(value = 5, message = "睡眠质量最大为 5")
    private Integer sleepQuality;

    /** 压力水平 1-5，前端传字符串，Jackson 自动转 Integer */
    @Min(value = 1, message = "压力水平最小为 1")
    @Max(value = 5, message = "压力水平最大为 5")
    private Integer stressLevel;
}
