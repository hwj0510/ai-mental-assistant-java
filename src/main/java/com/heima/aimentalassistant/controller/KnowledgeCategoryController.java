package com.heima.aimentalassistant.controller;

import com.heima.aimentalassistant.common.results.Result;
import com.heima.aimentalassistant.pojo.vo.CategoryVO;
import com.heima.aimentalassistant.service.KnowledgeCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/knowledge/category")
public class KnowledgeCategoryController {

    @Autowired
    private KnowledgeCategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        log.info("========== GET /knowledge/category/tree ==========");
        return Result.success(categoryService.listAll());
    }
}