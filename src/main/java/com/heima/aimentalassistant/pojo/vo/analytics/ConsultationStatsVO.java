package com.heima.aimentalassistant.pojo.vo.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 咨询统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationStatsVO {
    /** 会话总数 */
    private Integer totalSessions;
    /** 平均会话时长（分钟） */
    private Integer avgDurationMinutes;
    /** 近 7 天每日趋势 */
    private List<ConsultationDailyItem> dailyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationDailyItem {
        /** 日期 YYYY-MM-DD */
        private String date;
        /** 当日会话数 */
        private Integer sessionCount;
        /** 当日参与用户数 */
        private Integer userCount;
    }
}