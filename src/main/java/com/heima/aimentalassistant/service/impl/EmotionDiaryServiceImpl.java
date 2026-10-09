package com.heima.aimentalassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.mapper.EmotionDiaryMapper;
import com.heima.aimentalassistant.mapper.UserMapper;
import com.heima.aimentalassistant.pojo.dto.EmotionDiaryQueryDTO;
import com.heima.aimentalassistant.pojo.dto.EmotionDiarySubmitDTO;
import com.heima.aimentalassistant.pojo.entity.EmotionDiary;
import com.heima.aimentalassistant.pojo.entity.User;
import com.heima.aimentalassistant.pojo.vo.AdminEmotionDiaryVO;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;
import com.heima.aimentalassistant.service.EmotionDiaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EmotionDiaryServiceImpl implements EmotionDiaryService {

    @Autowired
    private EmotionDiaryMapper emotionDiaryMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResultVO<EmotionDiary> getHistory(Long userId, EmotionDiaryQueryDTO queryDTO) {
        // 1. 页码参数兜底
        int pageNum = (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) ? 1 : queryDTO.getPageNum();
        int pageSize = (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) ? 10 : queryDTO.getPageSize();

        // 2. 构造查询条件
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionDiary::getUserId, userId);

        // 日期范围
        LocalDate startDate = parseDate(queryDTO.getStartDate(), "startDate");
        LocalDate endDate = parseDate(queryDTO.getEndDate(), "endDate");

        if (startDate != null) {
            wrapper.ge(EmotionDiary::getDiaryDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(EmotionDiary::getDiaryDate, endDate);
        }

        // 3. 查总数
        long total = emotionDiaryMapper.selectCount(wrapper);

        // 4. 分页查询
        wrapper.orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId)
                .last("LIMIT " + (pageNum - 1) * pageSize + "," + pageSize);
        List<EmotionDiary> records = emotionDiaryMapper.selectList(wrapper);

        return PageResultVO.of(records, total);
    }

    @Override
    public EmotionDiary save(Long userId, EmotionDiarySubmitDTO dto) {
        LocalDateTime now = LocalDateTime.now();

        EmotionDiary entity = EmotionDiary.builder()
                .userId(userId)                              // 强制使用 token 中的 userId
                .diaryDate(dto.getDiaryDate())
                .moodScore(dto.getMoodScore())
                .dominantEmotion(dto.getDominantEmotion())
                .emotionTriggers(dto.getEmotionTriggers())
                .diaryContent(dto.getDiaryContent())
                .sleepQuality(dto.getSleepQuality())
                .stressLevel(dto.getStressLevel())
                .createAt(now)
                .updateAt(now)
                .build();

        emotionDiaryMapper.insert(entity);
        return entity;
    }

    /**
     * 解析 YYYY-MM-DD 格式的日期字符串
     */
    private LocalDate parseDate(String dateStr, String paramName) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException(paramName + " 日期格式错误，应为 YYYY-MM-DD");
        }
    }

    // ================ 管理员接口 ================

    @Override
    public PageResultVO<AdminEmotionDiaryVO> adminPage(
            String userId, String moodScoreRange, Integer currentPage, Integer size) {

        // 1. 分页参数兜底
        int pn = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        int ps = (size == null || size < 1) ? 10 : size;

        // 2. 构造查询条件
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();

        // userId 精确匹配（字符串 -> Long）
        Long uid = parseUserId(userId);
        if (uid != null) {
            wrapper.eq(EmotionDiary::getUserId, uid);
        }

        // moodScoreRange "min-max" 区间
        Integer[] range = parseMoodScoreRange(moodScoreRange);
        if (range != null) {
            wrapper.between(EmotionDiary::getMoodScore, range[0], range[1]);
        }

        // 3. 查总数
        long total = emotionDiaryMapper.selectCount(wrapper);

        // 4. 排序：diaryDate 倒序，同日期按 id 倒序
        wrapper.orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId)
                .last("LIMIT " + (pn - 1) * ps + "," + ps);

        List<EmotionDiary> records = emotionDiaryMapper.selectList(wrapper);

        // 5. 批量 JOIN 用户信息（用循环查 UserMapper，一般 list 实现）
        List<AdminEmotionDiaryVO> voList = new ArrayList<>();
        for (EmotionDiary d : records) {
            User u = d.getUserId() != null ? userMapper.selectById(d.getUserId()) : null;
            voList.add(AdminEmotionDiaryVO.builder()
                    .id(d.getId())
                    .userId(d.getUserId())
                    .username(u != null ? u.getUsername() : null)
                    .nickname(u != null ? u.getNickname() : null)
                    .diaryDate(d.getDiaryDate())
                    .moodScore(d.getMoodScore())
                    .dominantEmotion(d.getDominantEmotion())
                    .sleepQuality(d.getSleepQuality())
                    .stressLevel(d.getStressLevel())
                    .emotionTriggers(d.getEmotionTriggers())
                    .diaryContent(d.getDiaryContent())
                    .aiEmotionAnalysis(d.getAiEmotionAnalysis())
                    .createdAt(d.getCreateAt())
                    .updatedAt(d.getUpdateAt())
                    .build());
        }

        return PageResultVO.of(voList, total);
    }

    @Override
    public void adminDelete(Long id) {
        EmotionDiary diary = emotionDiaryMapper.selectById(id);
        if (diary == null) {
            throw new BusinessException("情绪日记不存在");
        }
        emotionDiaryMapper.deleteById(id);
    }

    /**
     * 解析 userId 字符串 -> Long。空串/null 返回 null（忽略）。
     */
    private Long parseUserId(String userIdStr) {
        if (!StringUtils.hasText(userIdStr)) {
            return null;
        }
        try {
            return Long.parseLong(userIdStr.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("userId 格式错误，应为数字");
        }
    }

    /**
     * 解析 "min-max" 格式的情绪评分区间字符串。格式错误抛 BusinessException。
     */
    private Integer[] parseMoodScoreRange(String rangeStr) {
        if (!StringUtils.hasText(rangeStr)) {
            return null;
        }
        String[] parts = rangeStr.trim().split("-");
        if (parts.length != 2) {
            throw new BusinessException("moodScoreRange 格式错误，应为 min-max");
        }
        try {
            int min = Integer.parseInt(parts[0].trim());
            int max = Integer.parseInt(parts[1].trim());
            if (min > max) {
                throw new BusinessException("moodScoreRange min 不能大于 max");
            }
            return new Integer[]{min, max};
        } catch (NumberFormatException e) {
            throw new BusinessException("moodScoreRange 格式错误，min/max 应为数字");
        }
    }
}