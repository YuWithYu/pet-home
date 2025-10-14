package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Feedback;
import com.pethome.service.FeedbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedbacks")
@Api(tags = "评价反馈管理")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/page")
    @ApiOperation("分页查询评价反馈")
    public Result<IPage<Feedback>> getFeedbackPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Feedback> page = new Page<>(pageNo, pageSize);
        IPage<Feedback> result = feedbackService.getFeedbackList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建评价反馈")
    public Result<Feedback> createFeedback(@RequestBody Feedback feedback) {
        return Result.success(feedbackService.createFeedback(feedback));
    }

    @PutMapping("/update")
    @ApiOperation("更新评价反馈")
    public Result<Feedback> updateFeedback(@RequestBody Feedback feedback) {
        return Result.success(feedbackService.updateFeedback(feedback));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除评价反馈")
    public Result<Boolean> deleteFeedback(@PathVariable Long id) {
        return Result.success(feedbackService.deleteFeedback(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取评价反馈详情")
    public Result<Feedback> getFeedbackDetail(@PathVariable Long id) {
        return Result.success(feedbackService.getFeedbackById(id));
    }
}


