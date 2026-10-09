package com.heima.aimentalassistant.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleListVO {
    private String id;
    private String title;
    private Long categoryId;
    /** ⚠️ JOIN knowledge_category 返回 category_name，用户端列表直接显示 */
    private String categoryName;
    private String authorName;
    private Integer readCount;
    private Integer status;
    private LocalDateTime updatedAt;
}