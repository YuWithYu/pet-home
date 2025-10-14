package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Feedback;

public interface FeedbackService {
    IPage<Feedback> getFeedbackList(Page<Feedback> page);
    Feedback createFeedback(Feedback feedback);
    Feedback updateFeedback(Feedback feedback);
    boolean deleteFeedback(Long id);
    Feedback getFeedbackById(Long id);
}

