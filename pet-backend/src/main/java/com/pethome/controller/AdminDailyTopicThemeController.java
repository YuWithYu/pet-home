package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.Result;
import com.pethome.entity.DailyTopicTheme;
import com.pethome.service.DailyTopicThemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员-宠物专题主题分类（增删改查，与小程序 Tab 同步）
 */
@RestController
@RequestMapping("/api/admin/daily-topic-themes")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "管理员-专题主题分类")
public class AdminDailyTopicThemeController {

    @Autowired
    private DailyTopicThemeService dailyTopicThemeService;

    @GetMapping
    @ApiOperation("获取主题分类列表（含禁用，按排序）")
    public Result<List<DailyTopicTheme>> list() {
        List<DailyTopicTheme> list = dailyTopicThemeService.list(
            new QueryWrapper<DailyTopicTheme>().orderByAsc("sort_order")
        );
        return Result.success(list);
    }

    @PostMapping
    @ApiOperation("新增主题分类")
    public Result<DailyTopicTheme> create(@RequestBody DailyTopicTheme theme) {
        if (theme.getCode() == null || theme.getCode().trim().isEmpty()) {
            return Result.error("code 不能为空");
        }
        if (theme.getName() == null || theme.getName().trim().isEmpty()) {
            return Result.error("name 不能为空");
        }
        theme.setId(null);
        if (theme.getSortOrder() == null) theme.setSortOrder(0);
        if (theme.getStatus() == null) theme.setStatus(1);
        dailyTopicThemeService.save(theme);
        return Result.success(theme);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新主题分类")
    public Result<DailyTopicTheme> update(@PathVariable Long id, @RequestBody DailyTopicTheme theme) {
        DailyTopicTheme existing = dailyTopicThemeService.getById(id);
        if (existing == null) {
            return Result.error("主题不存在");
        }
        theme.setId(id);
        if (theme.getName() != null && !theme.getName().trim().isEmpty()) existing.setName(theme.getName().trim());
        if (theme.getCode() != null && !theme.getCode().trim().isEmpty()) existing.setCode(theme.getCode().trim());
        if (theme.getSortOrder() != null) existing.setSortOrder(theme.getSortOrder());
        if (theme.getStatus() != null) existing.setStatus(theme.getStatus());
        dailyTopicThemeService.updateById(existing);
        return Result.success(existing);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除主题分类")
    public Result<String> delete(@PathVariable Long id) {
        if (!dailyTopicThemeService.removeById(id)) {
            return Result.error("删除失败或不存在");
        }
        return Result.success("删除成功");
    }
}
