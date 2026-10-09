package com.heima.aimentalassistant.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_category")
@Builder
public class KnowledgeCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("category_name")
    private String categoryName;

    @TableField("description")
    private String description;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("create_at")
    private LocalDateTime createAt;

    @TableField("update_at")
    private LocalDateTime updateAt;
}
