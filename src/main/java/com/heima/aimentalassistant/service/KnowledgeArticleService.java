package com.heima.aimentalassistant.service;

import com.heima.aimentalassistant.pojo.dto.ArticleQueryDTO;
import com.heima.aimentalassistant.pojo.dto.ArticleSubmitDTO;
import com.heima.aimentalassistant.pojo.vo.ArticleDetailVO;
import com.heima.aimentalassistant.pojo.vo.ArticleListVO;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;

public interface KnowledgeArticleService {
    /**
     * 分页查询（两端共用，query.admin=true 时为管理员视角）
     *  - 管理员：返回所有状态文章，支持 title/categoryId/status 筛选
     *  - 普通用户/游客：强制 status=1，忽略 status 参数
     */
    PageResultVO<ArticleListVO> pageList(ArticleQueryDTO query);

    /**
     * 详情（两端共用）
     * @param id       文章 ID
     * @param isAdmin  管理员=true 时可以看草稿/下线；普通用户/游客只能看已发布
     * @param readOnly true=纯查询不自增阅读量（管理端编辑回显用）；false=普通访问自增
     */
    ArticleDetailVO getById(String id, boolean isAdmin, boolean readOnly);

    /** 新增，authorName 从 token 解析 */
    void create(String authorName, ArticleSubmitDTO dto);

    /** 更新 */
    void update(ArticleSubmitDTO dto);

    /**
     * 发布 / 下线 / 草稿切换
     * ⚠️ status → 1（发布）时写入 publishedAt
     */
    void updateStatus(String id, Integer status);

    /** 删除（仅草稿可删） */
    void delete(String id);
}