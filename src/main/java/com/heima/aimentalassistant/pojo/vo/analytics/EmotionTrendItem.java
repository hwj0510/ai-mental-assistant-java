package com.heima.aimentalassistant.pojo.vo.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 情绪趋势图项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionTrendItem {
    /** 日期 YYYY-MM-DD */
    private String date;
    /** 当日平均情绪评分 */
    private Double avgMoodScore;
    /** 当日记录数 */
    private Integer recordCount;
}