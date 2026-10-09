package com.heima.aimentalassistant.controller.admin;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.heima.aimentalassistant.common.enums.UserType;
import com.heima.aimentalassistant.common.exception.BusinessException;
import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.common.utils.JWTUtils;
import com.heima.aimentalassistant.pojo.dto.ArticleQueryDTO;
import com.heima.aimentalassistant.pojo.dto.ArticleStatusDTO;
import com.heima.aimentalassistant.pojo.dto.ArticleSubmitDTO;
import com.heima.aimentalassistant.pojo.vo.ArticleDetailVO;
import com.heima.aimentalassistant.pojo.vo.ArticleListVO;
import com.heima.aimentalassistant.pojo.vo.PageResultVO;
import com.heima.aimentalassistant.service.KnowledgeArticleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

@RestController
@Slf4j
@RequestMapping("/api/knowledge/article")
public class KnowledgeArticleController {

    @Autowired
    private KnowledgeArticleService articleService;

    /** 从 token 校验管理员并返回 username */
    private String requireAdminAndGetUsername() {
        String token = JWTUtils.getCurrentToken();
        DecodedJWT decodedJWT = JWTUtils.validateToken(token);
        Integer roleType = decodedJWT.getClaim("roleType").asInt();
        if (!UserType.ADMIN.getCode().equals(roleType)) {
            throw new BusinessException("权限不足，仅管理员可操作");
        }
        return decodedJWT.getClaim("username").asString();
    }

    /**
     * 安全地判断当前调用者是否为管理员（无 token / 非法 token 都视为非管理员）
     */
    private boolean isAdmin() {
        String token = JWTUtils.getCurrentToken();
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            DecodedJWT decodedJWT = JWTUtils.validateToken(token);
            Integer roleType = decodedJWT.getClaim("roleType").asInt();
            return UserType.ADMIN.getCode().equals(roleType);
        } catch (Exception e) {
            return false;
        }
    }

    // ============== 1. 分页查询（两端共用） ==============

    @GetMapping("/page")
    public Result<PageResultVO<ArticleListVO>> page(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) Integer currentPage,
            @RequestParam(required = false) Integer size) {
        log.info("========== GET /knowledge/article/page ==========");
        boolean admin = isAdmin();
        log.info("[分页查询] callerIsAdmin={}, title={}, categoryId={}, status={}, sortField={}, sortDirection={}, page={}, size={}",
                admin, title, categoryId, status, sortField, sortDirection, currentPage, size);

        ArticleQueryDTO query = ArticleQueryDTO.builder()
                .title(title)
                .categoryId(categoryId)
                // ⚠️ 管理员才允许 status 筛选，普通用户/游客强制忽略
                .status(admin ? status : null)
                .sortField(sortField)
                .sortDirection(sortDirection)
                .currentPage(currentPage)
                .size(size)
                .admin(admin)
                .build();

        return Result.success(articleService.pageList(query));
    }

    // ============== 2. 详情（两端共用 + 权限矩阵 + 阅读量自增） ==============

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable String id) {
        log.info("========== GET /knowledge/article/{} ==========", id);
        boolean admin = isAdmin();
        log.info("[文章详情] callerIsAdmin={}, id={}", admin, id);

        // 管理员访问：编辑回显场景，不加阅读量（readOnly=true）
        // 普通用户/游客访问：自增阅读量（readOnly=false）
        ArticleDetailVO vo = articleService.getById(id, admin, admin);
        return Result.success(vo);
    }

    // ============== 3. 新增（管理员） ==============

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ArticleSubmitDTO dto) {
        log.info("========== POST /knowledge/article ==========");
        String username = requireAdminAndGetUsername();
        log.info("[新增文章] admin={}, id={}, title={}", username, dto.getId(), dto.getTitle());
        articleService.create(username, dto);
        return Result.success(null);
    }

    // ============== 4. 更新（管理员） ==============

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id,
                               @Valid @RequestBody ArticleSubmitDTO dto) {
        log.info("========== PUT /knowledge/article/{} ==========", id);
        requireAdminAndGetUsername();
        // 路径上的 id 优先，防止 body 传错 id
        dto.setId(id);
        articleService.update(dto);
        return Result.success(null);
    }

    // ============== 5. 发布 / 下线 / 草稿（管理员） ==============

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id,
                                     @Valid @RequestBody ArticleStatusDTO dto) {
        log.info("========== PUT /knowledge/article/{}/status ==========", id);
        requireAdminAndGetUsername();
        log.info("[更新状态] id={}, status={}", id, dto.getStatus());
        articleService.updateStatus(id, dto.getStatus());
        return Result.success(null);
    }

    // ============== 6. 删除（管理员，仅草稿可删） ==============

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        log.info("========== DELETE /knowledge/article/{} ==========", id);
        requireAdminAndGetUsername();
        articleService.delete(id);
        return Result.success(null);
    }
}