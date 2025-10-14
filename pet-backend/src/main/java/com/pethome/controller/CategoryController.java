package com.pethome.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Category;
import com.pethome.service.CategoryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/categories")
@Api(tags = "分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    @ApiOperation("分页查询分类")
    public Result<IPage<Category>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Category> page = new Page<>(pageNo, pageSize);
        IPage<Category> result = categoryService.getCategoryPage(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建分类")
    public Result<Category> createCategory(@RequestBody Category category) {
        // 这里应该有CategoryService的create方法
        return Result.success(category);
    }

    @PutMapping("/update")
    @ApiOperation("更新分类")
    public Result<Category> updateCategory(@RequestBody Category category) {
        // 这里应该有CategoryService的update方法
        return Result.success(category);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        // 这里应该有CategoryService的delete方法
        return Result.success(true);
    }
}
