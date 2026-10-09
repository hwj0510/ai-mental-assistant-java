package com.heima.aimentalassistant.controller;

import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.pojo.vo.FileUploadVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/file")
public class FileUploadController {

    /** 上传根路径（绝对路径），可在 application.yml 中配置 */
    @Value("${file.upload-dir:${user.dir}/uploads}")
    private String uploadDir;

    /** 允许的最大文件大小：5MB */
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    /** 允许的图片后缀 */
    private static final String[] ALLOWED_EXT = {"jpg", "jpeg", "png", "gif", "webp"};

    private static final DateTimeFormatter DIR_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @PostMapping("/upload")
    public Result<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "businessId", required = false) String businessId,
            @RequestParam(value = "businessField", required = false) String businessField) {

        log.info("========== POST /file/upload ==========");
        log.info("[文件上传] originalName={}, size={}, businessType={}, businessId={}, businessField={}",
                file.getOriginalFilename(), file.getSize(), businessType, businessId, businessField);

        // 1. 基本校验
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件大小不能超过 5MB");
        }

        // 2. 校验后缀
        String originalName = file.getOriginalFilename();
        assert originalName != null;
        int dotIdx = originalName.lastIndexOf('.');
        if (dotIdx < 0) {
            throw new BusinessException("文件名无效");
        }
        String ext = originalName.substring(dotIdx + 1).toLowerCase();
        boolean allowed = false;
        for (String e : ALLOWED_EXT) {
            if (e.equals(ext)) { allowed = true; break; }
        }
        if (!allowed) {
            throw new BusinessException("仅支持 jpg/jpeg/png/gif/webp 格式");
        }

        // 3. 生成存储路径：/upload/yyyy/MM/dd/UUID.ext
        String datePath = LocalDate.now().format(DIR_FMT);
        String relativeDir = "/upload/" + datePath;
        String absoluteDir = uploadDir + File.separator + "upload" + File.separator + datePath;

        // 确保目录存在
        File dir = new File(absoluteDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }

        // 4. 生成新文件名（UUID 去横线）
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File dest = new File(dir, newFilename);

        // 5. 保存文件
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException("文件保存失败");
        }

        // 6. 返回相对路径（前端自己拼 fileBaseUrl）
        String filePath = relativeDir + "/" + newFilename;
        log.info("[文件上传成功] filePath={}", filePath);

        return Result.success(FileUploadVO.builder().filePath(filePath).build());
    }
}