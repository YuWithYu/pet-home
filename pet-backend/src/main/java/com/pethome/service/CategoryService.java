package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Category;

import java.util.List;

public interface CategoryService {
    IPage<Category> getCategoryPage(Page<Category> page);
    Category createCategory(Category category);
    Category updateCategory(Category category);
    boolean deleteCategory(Long id);
    List<Category> getAllCategories();
}


