package com.heima.aimentalassistant.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章状态更新 Body
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleStatusDTO {
    /** 0-草稿 1-已发布 2-已下线 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}