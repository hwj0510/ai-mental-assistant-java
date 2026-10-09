package com.heima.aimentalassistant.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情（字段最全，管理端和用户端都用这一个）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailVO {
    private String id;
    private String title;
    /** HTML 内容 */
    private String content;
    private String coverImage;
    private Long categoryId;
    private String summary;
    /** 标签，逗号分隔字符串 */
    private String tags;

    /** === 用户端额外字段 === */
    private String categoryName;
    private String authorName;
    private Integer readCount;
    /** 标签数组（给前端直接回显） */
    private List<String> tagArray;

    private Integer status;
    private LocalDateTime updatedAt;
}
