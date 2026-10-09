package com.heima.aimentalassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.aimentalassistant.common.enums.UserStatus;
import com.heima.aimentalassistant.mapper.ConsultationMessageMapper;
import com.heima.aimentalassistant.mapper.ConsultationSessionMapper;
import com.heima.aimentalassistant.mapper.EmotionDiaryMapper;
import com.heima.aimentalassistant.mapper.UserMapper;
import com.heima.aimentalassistant.pojo.entity.ConsultationMessage;
import com.heima.aimentalassistant.pojo.entity.ConsultationSession;
import com.heima.aimentalassistant.pojo.entity.EmotionDiary;
import com.heima.aimentalassistant.pojo.entity.User;
import com.heima.aimentalassistant.pojo.vo.analytics.*;
import com.heima.aimentalassistant.service.DataAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class DataAnalyticsServiceImpl implements DataAnalyticsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmotionDiaryMapper emotionDiaryMapper;
    @Autowired
    private ConsultationSessionMapper sessionMapper;
    @Autowired
    private ConsultationMessageMapper messageMapper;


    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public AnalyticsOverviewVO getOverview() {
        return AnalyticsOverviewVO.builder()
                .systemOverview(buildSystemOverview())
                .emotionTrend(buildEmotionTrend())
                .consultationStats(buildConsultationStats())
                .userActivity(buildUserActivity())
                .build();
    }

    // ==================== 系统总览卡片 ====================

    private SystemOverviewVO buildSystemOverview() {
        // 用户数
        long totalUsers = userMapper.selectCount(null);
        long activeUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, UserStatus.NORMAL.getCode()));

        // 情绪日志
        long totalDiaries = emotionDiaryMapper.selectCount(null);
        LocalDate today = LocalDate.now();
        long todayNewDiaries = emotionDiaryMapper.selectCount(
                new LambdaQueryWrapper<EmotionDiary>().eq(EmotionDiary::getDiaryDate, today));

        // 会话
        long totalSessions = sessionMapper.selectCount(null);
        long todayNewSessions = sessionMapper.selectCount(
                new LambdaQueryWrapper<ConsultationSession>()
                        .apply("DATE(started_at) = {0}", today));

        // 平均情绪评分（排除 null）
        QueryWrapper<EmotionDiary> avgW = new QueryWrapper<>();
        avgW.select("AVG(mood_score)").isNotNull("mood_score");
        Double avgMoodScore = emotionDiaryMapper.selectObjs(avgW).stream()
                .filter(Objects::nonNull)
                .map(o -> ((Number) o).doubleValue())
                .findFirst().orElse(null);

        return SystemOverviewVO.builder()
                .totalUsers((int) totalUsers)
                .activeUsers((int) activeUsers)
                .totalDiaries((int) totalDiaries)
                .todayNewDiaries((int) todayNewDiaries)
                .totalSessions((int) totalSessions)
                .todayNewSessions((int) todayNewSessions)
                .avgMoodScore(avgMoodScore != null ? Math.round(avgMoodScore * 10.0) / 10.0 : 0.0)
                .build();
    }

    // ==================== 情绪趋势（近 7 天） ====================

    private List<EmotionTrendItem> buildEmotionTrend() {
        List<EmotionTrendItem> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DATE_FMT);

            QueryWrapper<EmotionDiary> avgW = new QueryWrapper<>();
            avgW.select("AVG(mood_score)").eq("diary_date", date).isNotNull("mood_score");
            Double avgScore = emotionDiaryMapper.selectObjs(avgW).stream()
                    .filter(Objects::nonNull)
                    .map(o -> ((Number) o).doubleValue())
                    .findFirst().orElse(null);

            long count = emotionDiaryMapper.selectCount(
                    new LambdaQueryWrapper<EmotionDiary>().eq(EmotionDiary::getDiaryDate, date));

            result.add(EmotionTrendItem.builder()
                    .date(dateStr)
                    .avgMoodScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0)
                    .recordCount((int) count)
                    .build());
        }
        return result;
    }

    // ==================== 咨询统计 ====================

    private ConsultationStatsVO buildConsultationStats() {
        long totalSessions = sessionMapper.selectCount(null);

        // 平均会话时长：每个 session 最后一条消息时间 - startedAt
        List<ConsultationSession> all = sessionMapper.selectList(null);
        long totalMin = 0;
        int valid = 0;
        for (ConsultationSession s : all) {
            ConsultationMessage lastMsg = messageMapper.selectOne(
                    new LambdaQueryWrapper<ConsultationMessage>()
                            .eq(ConsultationMessage::getSessionId, s.getId())
                            .orderByDesc(ConsultationMessage::getCreatedAt)
                            .last("limit 1"));
            LocalDateTime end = lastMsg != null ? lastMsg.getCreatedAt() : s.getStartedAt();
            if (s.getStartedAt() != null && end != null) {
                totalMin += Math.max(0, Duration.between(s.getStartedAt(), end).toMinutes());
                valid++;
            }
        }
        int avgDuration = valid > 0 ? (int) Math.round((double) totalMin / valid) : 0;

        return ConsultationStatsVO.builder()
                .totalSessions((int) totalSessions)
                .avgDurationMinutes(avgDuration)
                .dailyTrend(buildConsultationDailyTrend())
                .build();
    }

    private List<ConsultationStatsVO.ConsultationDailyItem> buildConsultationDailyTrend() {
        List<ConsultationStatsVO.ConsultationDailyItem> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DATE_FMT);

            long sessionCount = sessionMapper.selectCount(
                    new LambdaQueryWrapper<ConsultationSession>()
                            .apply("DATE(started_at) = {0}", date));

            QueryWrapper<ConsultationSession> uw = new QueryWrapper<>();
            uw.select("DISTINCT user_id").apply("DATE(started_at) = {0}", date);
            long userCount = sessionMapper.selectObjs(uw).size();

            result.add(ConsultationStatsVO.ConsultationDailyItem.builder()
                    .date(dateStr)
                    .sessionCount((int) sessionCount)
                    .userCount((int) userCount)
                    .build());
        }
        return result;
    }

    // ==================== 用户活跃度（近 7 天） ====================

    private List<UserActivityItem> buildUserActivity() {
        List<UserActivityItem> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DATE_FMT);

            // 新增注册用户
            long newUsers = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().apply("DATE(create_at) = {0}", date));

            // 当日写日记的用户数（DISTINCT）
            QueryWrapper<EmotionDiary> dw = new QueryWrapper<>();
            dw.select("DISTINCT user_id").eq("diary_date", date);
            List<Object> diaryUserIds = emotionDiaryMapper.selectObjs(dw);

            // 当日咨询用户数（DISTINCT）
            QueryWrapper<ConsultationSession> cw = new QueryWrapper<>();
            cw.select("DISTINCT user_id").apply("DATE(started_at) = {0}", date);
            List<Object> consultUserIds = sessionMapper.selectObjs(cw);

            // 活跃用户 = 日记用户 ∪ 咨询用户（去重）
            Set<Object> union = new HashSet<>();
            union.addAll(diaryUserIds);
            union.addAll(consultUserIds);

            result.add(UserActivityItem.builder()
                    .date(dateStr)
                    .activeUsers(union.size())
                    .newUsers((int) newUsers)
                    .diaryUsers(diaryUserIds.size())
                    .consultationUsers(consultUserIds.size())
                    .build());
        }
        return result;
    }
}