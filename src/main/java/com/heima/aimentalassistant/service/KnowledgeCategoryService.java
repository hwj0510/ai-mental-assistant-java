package com.heima.aimentalassistant.service;

import com.heima.aimentalassistant.pojo.vo.CategoryVO;

import java.util.List;

public interface KnowledgeCategoryService {
    /** 返回所有分类（按 sortOrder 升序） */
    List<CategoryVO> listAll();
}