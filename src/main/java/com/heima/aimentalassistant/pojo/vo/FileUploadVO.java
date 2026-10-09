package com.heima.aimentalassistant.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVO {
    /** 相对路径，如 /upload/2026/10/xxx.jpg */
    private String filePath;
}
