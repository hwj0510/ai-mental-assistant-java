package com.heima.aimentalassistant.pojo.vo.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据看板总览（最外层 data）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsOverviewVO {
    /** 系统总览卡片数据 */
    private SystemOverviewVO systemOverview;
    /** 近 7 天情绪趋势 */
    private List<EmotionTrendItem> emotionTrend;
    /** 咨询统计 */
    private ConsultationStatsVO consultationStats;
    /** 近 7 天用户活跃度 */
    private List<UserActivityItem> userActivity;
}