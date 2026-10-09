package com.heima.aimentalassistant.controller.admin;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.heima.aimentalassistant.common.enums.UserType;
import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.common.utils.JWTUtils;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;
import com.heima.aimentalassistant.pojo.vo.SessionListVO;
import com.heima.aimentalassistant.pojo.vo.SessionMessageSimpleVO;
import com.heima.aimentalassistant.service.AIChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/psychological-chat/admin")
public class AdminConsultationController {

    @Autowired
    private AIChatService aiChatService;

    /** 校验管理员权限 */
    private void requireAdmin() {
        String token = JWTUtils.getCurrentToken();
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Integer roleType = decodedJWT.getClaim("roleType").asInt();
        if (!UserType.ADMIN.getCode().equals(roleType)) {
            throw new BusinessException("权限不足，仅管理员可访问");
        }
    }

    // ============== 1. 管理员会话列表 ==============

    @GetMapping("/sessions")
    public Result<PageResultVO<SessionListVO>> listSessions(
            @RequestParam(required = false) Integer currentPage,
            @RequestParam(required = false) Integer size) {
        log.info("========== GET /api/psychological-chat/admin/sessions ==========");
        requireAdmin();
        log.info("[管理员会话列表] currentPage={}, size={}", currentPage, size);
        return Result.success(aiChatService.adminListSessions(currentPage, size));
    }

    // ============== 2. 管理员查看会话消息详情 ==============

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<SessionMessageSimpleVO>> listMessages(@PathVariable Long sessionId) {
        log.info("========== GET /api/psychological-chat/admin/sessions/{}/messages ==========", sessionId);
        requireAdmin();
        return Result.success(aiChatService.adminListSessionMessages(sessionId));
    }
}

