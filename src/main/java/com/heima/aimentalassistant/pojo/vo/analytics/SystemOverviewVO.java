package com.heima.aimentalassistant.pojo.vo.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统总览卡片
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOverviewVO {
    /** 总用户数 */
    private Integer totalUsers;
    /** 活跃用户数（status=1 正常状态） */
    private Integer activeUsers;
    /** 情绪日志总数 */
    private Integer totalDiaries;
    /** 今日新增日志数 */
    private Integer todayNewDiaries;
    /** 咨询会话总数 */
    private Integer totalSessions;
    /** 今日新增会话数 */
    private Integer todayNewSessions;
    /** 平均情绪评分 */
    private Double avgMoodScore;
}