package com.heima.aimentalassistant.controller.admin;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.heima.aimentalassistant.common.enums.UserType;
import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.common.utils.JWTUtils;
import com.heima.aimentalassistant.pojo.vo.analytics.AnalyticsOverviewVO;
import com.heima.aimentalassistant.service.DataAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/data-analytics")
public class DataAnalyticsController {

    @Autowired
    private DataAnalyticsService dataAnalyticsService;

    @GetMapping("/overview")
    public Result<AnalyticsOverviewVO> overview() {
        log.info("========== 请求进入 GET /api/data-analytics/overview ==========");

        // 1. 从 token 获取当前用户，校验管理员权限
        String token = JWTUtils.getCurrentToken();
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        Integer roleType = decodedJWT.getClaim("roleType").asInt();

        if (!UserType.ADMIN.getCode().equals(roleType)) {
            throw new BusinessException("权限不足，仅管理员可访问数据看板");
        }

        log.info("[数据看板] 管理员 userId: {} 访问概览数据", userId);

        // 2. 查询并返回
        AnalyticsOverviewVO result = dataAnalyticsService.getOverview();
        return Result.success(result);
    }
}