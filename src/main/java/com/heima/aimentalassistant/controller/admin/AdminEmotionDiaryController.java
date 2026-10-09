package com.heima.aimentalassistant.controller.admin;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.heima.aimentalassistant.common.enums.UserType;
import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.common.utils.JWTUtils;
import com.heima.aimentalassistant.pojo.vo.AdminEmotionDiaryVO;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;
import com.heima.aimentalassistant.service.EmotionDiaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/emotion-diary/admin")
public class AdminEmotionDiaryController {

    @Autowired
    private EmotionDiaryService emotionDiaryService;

    /** 校验管理员权限（与 AdminConsultationController 保持一致） */
    private void requireAdmin() {
        String token = JWTUtils.getCurrentToken();
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Integer roleType = decodedJWT.getClaim("roleType").asInt();
        if (!UserType.ADMIN.getCode().equals(roleType)) {
            throw new BusinessException("权限不足，仅管理员可访问");
        }
    }

    // ============== 接口 1：分页查询 ==============

    @GetMapping("/page")
    public Result<PageResultVO<AdminEmotionDiaryVO>> page(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String moodScoreRange,
            @RequestParam(required = false) Integer currentPage,
            @RequestParam(required = false) Integer size) {

        log.info("========== GET /api/emotion-diary/admin/page ==========");
        requireAdmin();
        log.info("[管理员情绪日记分页] userId={}, moodScoreRange={}, currentPage={}, size={}",
                userId, moodScoreRange, currentPage, size);

        PageResultVO<AdminEmotionDiaryVO> result = emotionDiaryService.adminPage(
                userId, moodScoreRange, currentPage, size);
        return Result.success(result);
    }

    // ============== 接口 2：物理删除 ==============

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("========== DELETE /api/emotion-diary/admin/{} ==========", id);
        requireAdmin();
        log.info("[管理员删除情绪日记] id={}", id);

        emotionDiaryService.adminDelete(id);
        return Result.success(null);
    }
}