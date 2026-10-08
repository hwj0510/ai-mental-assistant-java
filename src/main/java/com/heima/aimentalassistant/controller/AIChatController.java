package com.heima.aimentalassistant.controller;

import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.heima.aimentalassistant.common.enums.ResultCode;
import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.common.utils.JWTUtils;
import com.heima.aimentalassistant.pojo.dto.AIChatDTO;
import com.heima.aimentalassistant.pojo.dto.AIChatStreamDTO;
import com.heima.aimentalassistant.pojo.vo.AIChatVO;
import com.heima.aimentalassistant.service.AIChatService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/psychological-chat")
public class AIChatController {
    @Autowired
    private AIChatService aiChatService;

    @PostMapping("/session/start")
    public Result<AIChatVO.StreamChatSession> startSession(@Valid @RequestBody AIChatDTO aiChatDTO) {
        log.info("========== 请求进入 POST /api/psychological-chat/session/start ==========");
        log.info("[会话创建] 请求参数: {}", aiChatDTO);

        String token = JWTUtils.getCurrentToken();
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        log.info("[会话创建] 当前用户 userId: {}", userId);

        AIChatVO.StreamChatSession result = aiChatService.startSession(userId, aiChatDTO);
      return Result.success(result);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody AIChatStreamDTO streamDTO) {
        log.info("========== 请求进入 POST /api/psychological-chat/stream ==========");
        log.info("[SSE流式对话] sessionId: {}, 用户消息: {}", streamDTO.getSessionId(), streamDTO.getUserMessage());

        String token = JWTUtils.getCurrentToken();
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        log.info("[SSE流式对话] 当前用户 userId: {}", userId);

        if (userId == null) {
            log.warn("[SSE流式对话] 用户未登录，返回错误事件");
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(),
                            ResultCode.UNAUTHORIZED.getMessage(), "用户未登录")))
                    .build());
        }

        log.info("[SSE流式对话] 开始调用 AI 服务...");
        return aiChatService.streamPsychologicalChat(streamDTO.getSessionId(), streamDTO.getUserMessage())
                .doOnSubscribe(sub -> log.info("[SSE流式对话] Flux 被订阅，开始向客户端推送数据"))
                .doOnNext(fragment -> log.debug("[SSE流式对话] 推送片段: {}", fragment))
                .doOnComplete(() -> log.info("[SSE流式对话] AI 响应完成，推送 done 事件"))
                .doOnError(err -> log.error("[SSE流式对话] AI 响应异常: {}", err.getMessage()))
                .map(fragment -> ServerSentEvent.<String>builder()
                        .event("message")
                        .data(JSONUtil.toJsonStr(Result.success(Map.of("content", fragment, "type", "normal"))))
                        .build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()))
                .delayElements(Duration.ofMillis(50));
    }
}