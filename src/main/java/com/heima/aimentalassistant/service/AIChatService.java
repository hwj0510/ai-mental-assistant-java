package com.heima.aimentalassistant.service;

import com.heima.aimentalassistant.pojo.dto.AIChatDTO;
import com.heima.aimentalassistant.pojo.dto.AIChatStreamDTO;
import com.heima.aimentalassistant.pojo.entity.ConsultationSession;
import com.heima.aimentalassistant.pojo.vo.AIChatVO;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;
import com.heima.aimentalassistant.pojo.vo.SessionListVO;
import com.heima.aimentalassistant.pojo.vo.SessionMessageSimpleVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AIChatService {
    AIChatVO.StreamChatSession startSession(Long userId, AIChatDTO aiChatDTO);

    Flux<String> streamPsychologicalChat(String sessionId, String userMessage);

    /**
     * 会话列表（分页，用户端），只返回指定用户的会话
     */
    PageResultVO<SessionListVO> listSessions(Long userId, Integer currentPage, Integer size);

    /**
     * 会话列表（分页，管理员端），返回所有用户的会话（不过滤 userId），按 lastMessageTime 倒序
     */
    PageResultVO<SessionListVO> adminListSessions(Integer currentPage, Integer size);

    /**
     * 删除会话（软删除，同时删除关联消息记录）
     */
    void deleteSession(Long userId, Long sessionId);

    /**
     * 管理员硬删除会话（不校验 userId，直接删会话 + 关联消息）
     */
    void adminDeleteSession(Long sessionId);

    /**
     * 获取会话消息详情列表（管理员可查看任意会话，普通用户只能看自己的）
     */
    List<SessionMessageSimpleVO> listSessionMessages(Long userId, Long sessionId);

    /**
     * 管理员获取任意会话消息详情（不校验 userId）
     */
    List<SessionMessageSimpleVO> adminListSessionMessages(Long sessionId);

    /**
     * 同步更新会话冗余字段（message_count / last_message_content / last_message_time）
     * 每次新增消息后调用
     */
    void updateSessionSummary(Long sessionId);
}