package com.heima.aimentalassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.mapper.KnowledgeArticleMapper;
import com.heima.aimentalassistant.mapper.KnowledgeCategoryMapper;
import com.heima.aimentalassistant.pojo.dto.ArticleQueryDTO;
import com.heima.aimentalassistant.pojo.dto.ArticleSubmitDTO;
import com.heima.aimentalassistant.pojo.entity.KnowledgeArticle;
import com.heima.aimentalassistant.pojo.entity.KnowledgeCategory;
import com.heima.aimentalassistant.pojo.vo.ArticleDetailVO;
import com.heima.aimentalassistant.pojo.vo.ArticleListVO;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;
import com.heima.aimentalassistant.service.KnowledgeArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class KnowledgeArticleServiceImpl implements KnowledgeArticleService {

    @Autowired
    private KnowledgeArticleMapper articleMapper;
    @Autowired
    private KnowledgeCategoryMapper categoryMapper;

    @Override
    public PageResultVO<ArticleListVO> pageList(ArticleQueryDTO query) {
        int pn = query.getCurrentPage() == null || query.getCurrentPage() < 1 ? 1 : query.getCurrentPage();
        int ps = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();
        boolean isAdmin = Boolean.TRUE.equals(query.getAdmin());

        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        // === 角色过滤 ===
        if (!isAdmin) {
            // 普通用户/游客：强制只看已发布
            wrapper.eq(KnowledgeArticle::getStatus, 1);
        } else {
            // 管理员：按传入 status 筛选（可空）
            if (query.getStatus() != null) {
                wrapper.eq(KnowledgeArticle::getStatus, query.getStatus());
            }
        }
        // 标题模糊匹配
        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(KnowledgeArticle::getTitle, query.getTitle().trim());
        }
        // categoryId
        if (query.getCategoryId() != null) {
            wrapper.eq(KnowledgeArticle::getCategoryId, query.getCategoryId());
        }

        // === 动态排序（白名单校验 + 类型安全 Lambda，防 SQL 注入） ===
        String sf = query.getSortField();
        boolean desc = !"asc".equalsIgnoreCase(query.getSortDirection());
        if ("publishedAt".equals(sf)) {
            if (desc) wrapper.orderByDesc(KnowledgeArticle::getPublishedAt);
            else wrapper.orderByAsc(KnowledgeArticle::getPublishedAt);
        } else if ("readCount".equals(sf)) {
            if (desc) wrapper.orderByDesc(KnowledgeArticle::getReadCount);
            else wrapper.orderByAsc(KnowledgeArticle::getReadCount);
        } else if ("updatedAt".equals(sf)) {
            if (desc) wrapper.orderByDesc(KnowledgeArticle::getUpdateAt);
            else wrapper.orderByAsc(KnowledgeArticle::getUpdateAt);
        } else {
            // 非法 sortField 或未传：默认 updatedAt 倒序
            wrapper.orderByDesc(KnowledgeArticle::getUpdateAt);
        }

        long total = articleMapper.selectCount(wrapper);
        wrapper.last("LIMIT " + (pn - 1) * ps + "," + ps);
        List<KnowledgeArticle> list = articleMapper.selectList(wrapper);

        // === 批量查分类名 JOIN ===
        List<Long> categoryIds = list.stream()
                .map(KnowledgeArticle::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(KnowledgeCategory::getId, KnowledgeCategory::getCategoryName));

        List<ArticleListVO> records = list.stream().map(a -> ArticleListVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .categoryId(a.getCategoryId())
                .categoryName(a.getCategoryId() != null ? categoryNameMap.get(a.getCategoryId()) : null)
                .authorName(a.getAuthorName())
                .readCount(a.getReadCount())
                .status(a.getStatus())
                .updatedAt(a.getUpdateAt())
                .build()).collect(Collectors.toList());

        return PageResultVO.of(records, total);
    }

    @Override
    public ArticleDetailVO getById(String id, boolean isAdmin, boolean readOnly) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // === 权限矩阵：普通用户/游客只能看已发布 ===
        if (!isAdmin && (article.getStatus() == null || article.getStatus() != 1)) {
            throw new BusinessException("文章不存在");
        }

        // === 阅读量自增（普通访问才加，管理员编辑回显不加） ===
        if (!isAdmin && !readOnly) {
            incrementReadCount(id);
            // 同步更新内存中的值
            article.setReadCount((article.getReadCount() == null ? 0 : article.getReadCount()) + 1);
        }

        // === 关联查 categoryName ===
        String categoryName = null;
        if (article.getCategoryId() != null) {
            KnowledgeCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                categoryName = category.getCategoryName();
            }
        }

        // tags 字符串 → List<String>
        List<String> tagArray = null;
        if (StringUtils.hasText(article.getTags())) {
            tagArray = Arrays.stream(article.getTags().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }

        return ArticleDetailVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .coverImage(article.getCoverImage())
                .categoryId(article.getCategoryId())
                .summary(article.getSummary())
                .tags(article.getTags())
                .categoryName(categoryName)
                .authorName(article.getAuthorName())
                .readCount(article.getReadCount())
                .tagArray(tagArray)
                .status(article.getStatus())
                .updatedAt(article.getUpdateAt())
                .build();
    }

    /**
     * 原子自增阅读量：UPDATE ... SET read_count = read_count + 1
     * 避免并发下的丢失更新
     */
    private void incrementReadCount(String id) {
        LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KnowledgeArticle::getId, id)
                .setSql("read_count = COALESCE(read_count, 0) + 1");
        articleMapper.update(null, updateWrapper);
    }

    @Override
    public void create(String authorName, ArticleSubmitDTO dto) {
        // 前端必须传 UUID 作为主键
        if (!StringUtils.hasText(dto.getId())) {
            throw new BusinessException("文章ID（UUID）不能为空");
        }
        // 检查 ID 是否重复
        KnowledgeArticle existing = articleMapper.selectById(dto.getId());
        if (existing != null) {
            throw new BusinessException("文章ID已存在，请勿重复提交");
        }
        // 校验 categoryId 有效性
        if (dto.getCategoryId() != null) {
            KnowledgeCategory cat = categoryMapper.selectById(dto.getCategoryId());
            if (cat == null) {
                throw new BusinessException("所选分类不存在");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .coverImage(dto.getCoverImage())
                .categoryId(dto.getCategoryId())
                .authorName(authorName)
                .summary(dto.getSummary())
                .tags(dto.getTags())
                .readCount(0)       // 默认 0
                .status(0)         // 默认草稿
                .createAt(now)
                .updateAt(now)
                .build();

        articleMapper.insert(article);
    }

    @Override
    public void update(ArticleSubmitDTO dto) {
        if (!StringUtils.hasText(dto.getId())) {
            throw new BusinessException("文章ID不能为空");
        }
        KnowledgeArticle existing = articleMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("文章不存在");
        }
        if (dto.getCategoryId() != null) {
            KnowledgeCategory cat = categoryMapper.selectById(dto.getCategoryId());
            if (cat == null) {
                throw new BusinessException("所选分类不存在");
            }
        }

        KnowledgeArticle update = KnowledgeArticle.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .coverImage(dto.getCoverImage())
                .categoryId(dto.getCategoryId())
                .summary(dto.getSummary())
                .tags(dto.getTags())
                .updateAt(LocalDateTime.now())
                .build();

        articleMapper.updateById(update);
    }

    @Override
    public void updateStatus(String id, Integer status) {
        KnowledgeArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("文章不存在");
        }
        if (status != 0 && status != 1 && status != 2) {
            throw new BusinessException("无效的状态值");
        }
        LocalDateTime now = LocalDateTime.now();
        KnowledgeArticle update = KnowledgeArticle.builder()
                .id(id)
                .status(status)
                .updateAt(now)
                // ⚠️ status → 1（发布）时写入 publishedAt
                // 已有 publishedAt 则保留（首次发布时间有纪念意义），也可改为每次发布都刷新
                .publishedAt(status == 1 && existing.getPublishedAt() == null ? now : existing.getPublishedAt())
                .build();
        articleMapper.updateById(update);
    }

    @Override
    public void delete(String id) {
        KnowledgeArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("文章不存在");
        }
        // 已发布不可直接删除，需先下线再删
        if (existing.getStatus() != null && existing.getStatus() == 1) {
            throw new BusinessException("已发布的文章不可直接删除，请先下线再删除");
        }
        articleMapper.deleteById(id);
    }
}