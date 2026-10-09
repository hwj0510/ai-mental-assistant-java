package com.heima.aimentalassistant.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章分页查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleQueryDTO {
    /** 标题（模糊匹配） */
    private String title;
    /** 分类ID（可空/空串需忽略） */
    private Long categoryId;
    /** 状态 0-草稿 1-已发布 2-已下线（可空/空串需忽略） */
    private Integer status;
    /** 排序字段白名单：publishedAt / readCount / updatedAt（非法值会被忽略，默认 updatedAt） */
    private String sortField;
    /** 排序方向：asc / desc，默认 desc */
    private String sortDirection;
    /** 当前页，从 1 开始 */
    @Builder.Default
    private Integer currentPage = 1;
    /** 每页大小，默认 10 */
    @Builder.Default
    private Integer size = 10;

    /** === 角色上下文（Controller 注入） === */
    /** 管理员=true 时允许看全部状态，普通用户/游客强制 status=1 */
    private Boolean admin;
}