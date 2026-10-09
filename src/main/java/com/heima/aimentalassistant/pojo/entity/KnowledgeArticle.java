package com.heima.aimentalassistant.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_article")
@Builder
public class KnowledgeArticle {

    /** 主键为前端传入的 UUID 字符串 */
    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("cover_image")
    private String coverImage;

    @TableField("category_id")
    private Long categoryId;

    @TableField("author_name")
    private String authorName;

    @TableField("summary")
    private String summary;

    @TableField("tags")
    private String tags;

    @TableField("read_count")
    private Integer readCount;

    /** 0-草稿 1-已发布 2-已下线 */
    @TableField("status")
    private Integer status;

    /** 首次发布时间（status 变为 1 时写入，之后下线再发布可更新或保留） */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField("create_at")
    private LocalDateTime createAt;

    @TableField("update_at")
    private LocalDateTime updateAt;
}