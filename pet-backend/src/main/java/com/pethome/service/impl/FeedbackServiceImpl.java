package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Feedback;
import com.pethome.mapper.FeedbackMapper;
import com.pethome.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public IPage<Feedback> getFeedbackList(Page<Feedback> page) {
        return feedbackMapper.selectPage(page, null);
    }

    @Override
    public Feedback createFeedback(Feedback feedback) {
        feedbackMapper.insert(feedback);
        return feedback;
    }

    @Override
    public Feedback updateFeedback(Feedback feedback) {
        feedbackMapper.updateById(feedback);
        return feedback;
    }

    @Override
    public boolean deleteFeedback(Long id) {
        return feedbackMapper.deleteById(id) > 0;
    }

    @Override
    public Feedback getFeedbackById(Long id) {
        return feedbackMapper.selectById(id);
    }
}


