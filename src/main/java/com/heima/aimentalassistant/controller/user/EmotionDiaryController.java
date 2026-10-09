package com.heima.aimentalassistant.controller.user;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.common.utils.JWTUtils;
import com.heima.aimentalassistant.pojo.dto.EmotionDiaryQueryDTO;
import com.heima.aimentalassistant.pojo.dto.EmotionDiarySubmitDTO;
import com.heima.aimentalassistant.pojo.entity.EmotionDiary;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;
import jakarta.validation.Valid;
import com.heima.aimentalassistant.service.EmotionDiaryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {

    @Autowired
    private EmotionDiaryService emotionDiaryService;

    @GetMapping("/page")
    public Result<PageResultVO<EmotionDiary>> getEmotionDiaryPage(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {

        // 1. 从 Authorization 头解析 token，获取当前用户 ID
        String token = extractToken(request);
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();

        log.info("[情绪日志历史] userId: {}, pageNum: {}, pageSize: {}, startDate: {}, endDate: {}",
                userId, pageNum, pageSize, startDate, endDate);

        // 2. 构造查询参数
        EmotionDiaryQueryDTO queryDTO = new EmotionDiaryQueryDTO();
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);

        // 3. 查询并返回
        PageResultVO<EmotionDiary> result = emotionDiaryService.getHistory(userId, queryDTO);
        return Result.success(result);
    }

    /**
     * 从请求头提取 token，同时支持两种格式：
     *   - Authorization: <token>
     *   - Authorization: Bearer <token>
     *   - token: <token>  （兼容现有 JwtAuthenticationFilter 的读取方式）
     */
    private String extractToken(HttpServletRequest request) {
        // 优先读 Authorization 头
        String auth = request.getHeader("Authorization");
        if (auth != null && !auth.isBlank()) {
            String token = auth.trim();
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
            }
            return token;
        }
        // 兜底读 token 头（兼容现有方式）
        String token = request.getHeader("token");
        if (token != null && !token.isBlank()) {
            return token.trim();
        }
        throw new com.heima.aimentalassistant.common.exception.BusinessException("未获取到 token，请先登录");
    }

    @PostMapping
    public Result<EmotionDiary> addEmotionDiary(@Valid @RequestBody EmotionDiarySubmitDTO dto,
                                                HttpServletRequest request) {
        // 1. 从 token 获取当前用户 ID，忽略 body 中可能携带的 userId
        String token = extractToken(request);
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();

        log.info("[提交情绪日志] userId: {}, diaryDate: {}, moodScore: {}, dominantEmotion: {}",
                userId, dto.getDiaryDate(), dto.getMoodScore(), dto.getDominantEmotion());

        // 2. 保存
        EmotionDiary saved = emotionDiaryService.save(userId, dto);
        return Result.success(saved);
    }
}