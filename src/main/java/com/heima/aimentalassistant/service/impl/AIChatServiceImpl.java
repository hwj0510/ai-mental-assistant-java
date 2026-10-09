package com.heima.aimentalassistant.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.aimentalassistant.mapper.ConsultationMessageMapper;
import com.heima.aimentalassistant.mapper.ConsultationSessionMapper;
import com.heima.aimentalassistant.mapper.UserMapper;
import com.heima.aimentalassistant.pojo.dto.AIChatDTO;
import com.heima.aimentalassistant.pojo.entity.ConsultationMessage;
import com.heima.aimentalassistant.pojo.entity.ConsultationSession;
import com.heima.aimentalassistant.pojo.entity.PromptMessage;
import com.heima.aimentalassistant.pojo.entity.User;
import com.heima.aimentalassistant.pojo.vo.AIChatVO;
import com.heima.aimentalassistant.pojo.vo.ConsultationMessageVO;
import com.heima.aimentalassistant.service.AIChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;
import com.heima.aimentalassistant.pojo.vo.SessionListVO;
import com.heima.aimentalassistant.pojo.vo.SessionMessageSimpleVO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AIChatServiceImpl implements AIChatService {

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;
    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    public AIChatVO.StreamChatSession startSession(Long userId, AIChatDTO aiChatDTO) {
        //创建会话记录

        ConsultationSession session = createSession(userId, aiChatDTO);

        //将初始用户消息保存到message表中
        saveUsermessage(session.getId(), aiChatDTO.getInitialMessage(), null);

        //创建会话信息
        String sessionId ="session_"+session.getId();
        return new AIChatVO.StreamChatSession(
                sessionId,
                userId,
                aiChatDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L,//24小时
                1,
                "ACTIVE"
        );
    }

    public ConsultationSession createSession(Long userId, AIChatDTO aiChatDTO) {
        //验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user != null) {
            //创建会话记录
            ConsultationSession consultationSession = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(aiChatDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            //如果未提供标题
            if (StrUtil.isBlank(aiChatDTO.getSessionTitle())) {
                consultationSession.setSessionTitle("灵心AI助手 —— "+ DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm"));
            }
            //插入记录
            consultationSessionMapper.insert(consultationSession);
            return consultationSession;
        }
        return null;
    }

    public ConsultationMessage saveUsermessage(Long sessionId, String content, String emotionTag) {
        //创建消息记录
        ConsultationMessage message = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(1)
                .messageType(1)
                .content(content)
                .emotionTag(emotionTag)
                .createdAt(LocalDateTime.now())
                .build();
        //插入记录
        consultationMessageMapper.insert(message);
        // 同步更新会话冗余字段
        updateSessionSummary(sessionId);
        return message;
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {

        // 1. 校验 sessionId
        Long dbSessionId = extractSessionId(sessionId);
        if (dbSessionId == null) {
            return Flux.error(new RuntimeException("会话ID格式错误"));
        }

        // 2. 是否为初始消息，避免重复保存
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationMessage::getSessionId, dbSessionId);
        Long count = consultationMessageMapper.selectCount(queryWrapper);
        boolean isInitialMessage = false;
        Integer messageCount = count.intValue();
        if (messageCount == 1) {
            LambdaQueryWrapper<ConsultationMessage> lastMessageWrapper = new LambdaQueryWrapper<>();
            lastMessageWrapper.eq(ConsultationMessage::getSessionId, dbSessionId)
                    .orderByDesc(ConsultationMessage::getCreatedAt)
                    .last("limit 1");
            ConsultationMessage lastMessage = consultationMessageMapper.selectOne(lastMessageWrapper);
            ConsultationMessageVO lastMessageVO = lastMessage != null ? convertToResponseDTO(lastMessage) : null;
            if (lastMessageVO != null && lastMessageVO.getSenderType() == 1
                    && userMessage.equals(lastMessageVO.getContent())) {
                isInitialMessage = true;
            }
        }
        if (!isInitialMessage) {
            saveUsermessage(dbSessionId, userMessage, null);
        }

        // 3. 直接返回 AI 的 Flux —— 不要再套一层 Flux.create！
        String conversationId = "conversation_" + sessionId;
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(PromptMessage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
        ));

        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt(prompt)
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec
                        .param(chatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .doOnNext(fragment -> fullResponse.append(fragment))
                .doOnComplete(() -> {
                    String completeRes = fullResponse.toString();
                    saveAIMessage(dbSessionId, completeRes, "openai");
                    List<Message> aiMessages = new ArrayList<>();
                    aiMessages.add(new AssistantMessage(completeRes));
                    chatMemory.add(conversationId, aiMessages);
                })
                .doOnError(error -> {
                    System.err.println("AI 流式对话异常: " + error.getMessage());
                    error.printStackTrace();
                })
                // 兜底：AI 调用失败时推一条错误消息给前端，而不是让整个连接断掉
                .onErrorResume(error -> Flux.just("\n\n[系统提示：AI 服务暂时不可用，请稍后重试。原因：" + error.getMessage() + "]"));
    }
    public Long extractSessionId(String sessionId) {
        if(sessionId != null && sessionId.startsWith("session_")){
            return Long.parseLong(sessionId.substring("session_".length()));
        }
        return null;

    }

    private ConsultationMessageVO convertToResponseDTO(ConsultationMessage message) {
        if (message == null) {
            return null;
        }

        // 手动逐字段赋值，确保转换的准确性和可控性
        ConsultationMessageVO responseDTO = new ConsultationMessageVO();
        responseDTO.setId(message.getId());
        responseDTO.setSessionId(message.getSessionId());
        responseDTO.setSenderType(message.getSenderType());
        responseDTO.setMessageType(message.getMessageType());
        responseDTO.setContent(message.getContent());
        responseDTO.setEmotionTag(message.getEmotionTag());
        responseDTO.setAiModel(message.getAiModel());
        responseDTO.setCreatedAt(message.getCreatedAt());

        // 设置描述字段（通过实体方法获取）
        responseDTO.setSenderTypeDesc(message.getSenderTypeDesc());
        responseDTO.setMessageTypeDesc(message.getMessageTypeDesc());

        // 计算消息长度
        responseDTO.calculateContentLength();

        return responseDTO;
    }

    public ConsultationMessage saveAIMessage(Long sessionId, String content, String aiModel) {

        ConsultationMessage message = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(2)
                .messageType(1)
                .content(content)
                .aiModel(aiModel)
                .createdAt(LocalDateTime.now())
                .build();

        consultationMessageMapper.insert(message);
        // 同步更新会话冗余字段
        updateSessionSummary(sessionId);
        return message;
    }

    @Override
    public PageResultVO<SessionListVO> listSessions(Long userId, Integer currentPage, Integer size) {
        int pn = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        int ps = (size == null || size < 1) ? 10 : size;

        LambdaQueryWrapper<ConsultationSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(ConsultationSession::getUserId, userId)
                .eq(ConsultationSession::getDeleted, 0)
                // 优先按 lastMessageTime 倒序，没有就按 startedAt
                .orderByDesc(ConsultationSession::getLastMessageTime)
                .orderByDesc(ConsultationSession::getStartedAt);

        long total = consultationSessionMapper.selectCount(sessionWrapper);
        sessionWrapper.last("LIMIT " + (pn - 1) * ps + "," + ps);
        List<ConsultationSession> sessions = consultationSessionMapper.selectList(sessionWrapper);

        List<SessionListVO> records = sessions.stream().map(s -> SessionListVO.builder()
                .id(s.getId())
                .userNickname(null) // 用户端不需要昵称，留空
                .sessionTitle(s.getSessionTitle())
                .startedAt(s.getStartedAt())
                .lastMessageContent(s.getLastMessageContent())
                .messageCount(s.getMessageCount() != null ? s.getMessageCount() : 0)
                .lastMessageTime(s.getLastMessageTime())
                .build()).toList();

        return PageResultVO.of(records, total);
    }

    @Override
    public PageResultVO<SessionListVO> adminListSessions(Integer currentPage, Integer size) {
        int pn = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        int ps = (size == null || size < 1) ? 10 : size;

        // 管理员查看所有会话（包括软删除的）
        LambdaQueryWrapper<ConsultationSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.orderByDesc(ConsultationSession::getLastMessageTime)
                .orderByDesc(ConsultationSession::getStartedAt);

        long total = consultationSessionMapper.selectCount(sessionWrapper);
        sessionWrapper.last("LIMIT " + (pn - 1) * ps + "," + ps);
        List<ConsultationSession> sessions = consultationSessionMapper.selectList(sessionWrapper);

        // 关联查用户昵称
        List<SessionListVO> records = new ArrayList<>();
        for (ConsultationSession s : sessions) {
            String displayName = null;
            if (s.getUserId() != null) {
                User u = userMapper.selectById(s.getUserId());
                if (u != null) {
                    // 优先 nickname，没有 fallback 到 username
                    displayName = u.getDisplayName();
                }
            }
            records.add(SessionListVO.builder()
                    .id(s.getId())
                    .userNickname(displayName)
                    .sessionTitle(s.getSessionTitle())
                    .startedAt(s.getStartedAt())
                    .lastMessageContent(s.getLastMessageContent())
                    .messageCount(s.getMessageCount() != null ? s.getMessageCount() : 0)
                    .lastMessageTime(s.getLastMessageTime())
                    .build());
        }
        return PageResultVO.of(records, total);
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该会话");
        }

        // 软删除（管理端仍可见）
        ConsultationSession update = ConsultationSession.builder()
                .id(sessionId)
                .deleted(1)
                .build();
        consultationSessionMapper.updateById(update);
    }

    @Override
    @Transactional
    public void adminDeleteSession(Long sessionId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        // 先删关联消息，再删会话（硬删除）
        consultationMessageMapper.delete(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getSessionId, sessionId));
        consultationSessionMapper.deleteById(sessionId);
    }

    @Override
    public List<SessionMessageSimpleVO> listSessionMessages(Long userId, Long sessionId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null || session.getDeleted() != null && session.getDeleted() == 1) {
            throw new BusinessException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该会话");
        }
        return queryMessagesBySessionId(sessionId);
    }

    @Override
    public List<SessionMessageSimpleVO> adminListSessionMessages(Long sessionId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        return queryMessagesBySessionId(sessionId);
    }

    /** 公共：按 sessionId 查消息，按 createdAt 升序 */
    private List<SessionMessageSimpleVO> queryMessagesBySessionId(Long sessionId) {
        List<ConsultationMessage> messages = consultationMessageMapper.selectList(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getSessionId, sessionId)
                        .orderByAsc(ConsultationMessage::getCreatedAt));
        return messages.stream().map(msg -> SessionMessageSimpleVO.builder()
                .id(msg.getId())
                .senderType(msg.getSenderType())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .build()).toList();
    }

    @Override
    public void updateSessionSummary(Long sessionId) {
        // 消息总数
        long count = consultationMessageMapper.selectCount(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getSessionId, sessionId));
        // 最后一条消息
        ConsultationMessage last = consultationMessageMapper.selectOne(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getSessionId, sessionId)
                        .orderByDesc(ConsultationMessage::getCreatedAt)
                        .last("limit 1"));

        ConsultationSession update = ConsultationSession.builder()
                .id(sessionId)
                .messageCount((int) count)
                .lastMessageContent(last != null ? last.getContent() : null)
                .lastMessageTime(last != null ? last.getCreatedAt() : null)
                .build();
        consultationSessionMapper.updateById(update);
    }

}