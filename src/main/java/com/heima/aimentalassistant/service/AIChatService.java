package com.heima.aimentalassistant.service;

import com.heima.aimentalassistant.pojo.dto.AIChatDTO;
import com.heima.aimentalassistant.pojo.dto.AIChatStreamDTO;
import com.heima.aimentalassistant.pojo.entity.ConsultationSession;
import com.heima.aimentalassistant.pojo.vo.AIChatVO;
import reactor.core.publisher.Flux;

public interface AIChatService {
    AIChatVO.StreamChatSession startSession(Long userId, AIChatDTO aiChatDTO);

    Flux<String> streamPsychologicalChat(String sessionId, String userMessage);

}
