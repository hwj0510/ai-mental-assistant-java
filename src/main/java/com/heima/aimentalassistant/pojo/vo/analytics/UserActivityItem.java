package com.heima.aimentalassistant.pojo.vo.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户活跃度（每日）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityItem {
    /** 日期 YYYY-MM-DD */
    private String date;
    /** 当日活跃用户数（有日记或有咨询） */
    private Integer activeUsers;
    /** 当日新增注册用户数 */
    private Integer newUsers;
    /** 当日写日记的用户数 */
    private Integer diaryUsers;
    /** 当日有咨询会话的用户数 */
    private Integer consultationUsers;
}