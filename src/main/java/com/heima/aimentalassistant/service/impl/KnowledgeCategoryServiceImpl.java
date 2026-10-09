package com.heima.aimentalassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.aimentalassistant.mapper.KnowledgeCategoryMapper;
import com.heima.aimentalassistant.pojo.entity.KnowledgeCategory;
import com.heima.aimentalassistant.pojo.vo.CategoryVO;
import com.heima.aimentalassistant.service.KnowledgeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeCategoryServiceImpl implements KnowledgeCategoryService {

    @Autowired
    private KnowledgeCategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> listAll() {
        List<KnowledgeCategory> list = categoryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeCategory>()
                        .orderByAsc(KnowledgeCategory::getSortOrder));
        return list.stream().map(c -> CategoryVO.builder()
                .id(c.getId())
                .categoryName(c.getCategoryName())
                .build()).collect(Collectors.toList());
    }
}