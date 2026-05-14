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

import java.util.List;

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

    @GetMapping("/all")
    @ApiOperation("获取所有分类")
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @PostMapping("/create")
    @ApiOperation("创建分类")
    public Result<Category> createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return Result.success(created);
    }

    @PutMapping("/update")
    @ApiOperation("更新分类")
    public Result<Category> updateCategory(@RequestBody Category category) {
        if (category == null || category.getId() == null) {
            return Result.error("分类ID不能为空");
        }
        Category updated = categoryService.updateCategory(category);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        boolean ok = categoryService.deleteCategory(id);
        return Result.success(ok);
    }
}
