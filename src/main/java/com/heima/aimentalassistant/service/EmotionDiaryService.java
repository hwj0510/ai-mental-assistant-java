package com.heima.aimentalassistant.service;

import com.heima.aimentalassistant.pojo.dto.EmotionDiaryQueryDTO;
import com.heima.aimentalassistant.pojo.dto.EmotionDiarySubmitDTO;
import com.heima.aimentalassistant.pojo.entity.EmotionDiary;
import com.heima.aimentalassistant.pojo.vo.AdminEmotionDiaryVO;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;

public interface EmotionDiaryService {
    /**
     * 分页查询当前用户的情绪日志
     */
    PageResultVO<EmotionDiary> getHistory(Long userId, EmotionDiaryQueryDTO queryDTO);

    /**
     * 提交一条情绪日志
     * @param userId 当前用户ID（从 token 解析，忽略 DTO 中可能携带的 userId）
     * @param dto    提交内容
     * @return 插入后的完整实体（含 id）
     */
    EmotionDiary save(Long userId, EmotionDiarySubmitDTO dto);

    /**
     * 管理员：分页查询所有情绪日记（带用户信息 JOIN）
     * @param userId          可空，字符串解析后精确匹配
     * @param moodScoreRange  可空，"min-max" 格式区间字符串
     * @param currentPage     页码（从 1 开始）
     * @param size            每页条数
     */
    PageResultVO<AdminEmotionDiaryVO> adminPage(
            String userId, String moodScoreRange, Integer currentPage, Integer size);

    /**
     * 管理员：物理删除一条情绪日记
     */
    void adminDelete(Long id);
}