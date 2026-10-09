package com.heima.aimentalassistant.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章新增 / 更新 Body
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSubmitDTO {
    /** 新增时前端传 UUID 作为主键；更新时必传 */
    private String id;

    @NotBlank(message = "标题不能为空")
    private String title;

    /** HTML 内容 */
    private String content;

    /** 封面图片相对路径，可空 */
    private String coverImage;

    private Long categoryId;

    /** 摘要，可空 */
    private String summary;

    /** 标签，逗号分隔字符串，可空 */
    private String tags;
}
