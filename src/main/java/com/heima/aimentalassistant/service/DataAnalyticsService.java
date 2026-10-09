package com.heima.aimentalassistant.service;

import com.heima.aimentalassistant.pojo.vo.analytics.AnalyticsOverviewVO;

public interface DataAnalyticsService {
    /**
     * 获取数据看板总览（近 7 天趋势 + 总计卡片）
     */
    AnalyticsOverviewVO getOverview();
}