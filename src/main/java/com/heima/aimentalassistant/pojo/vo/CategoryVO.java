package com.heima.aimentalassistant.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识分类 VO（管理端和用户端共用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {
    private Long id;
    private String categoryName;
}