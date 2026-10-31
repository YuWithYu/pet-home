package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.TimeSlot;
import com.pethome.mapper.TimeSlotMapper;
import com.pethome.service.appointment.TimeSlotService;
import org.springframework.stereotype.Service;

@Service
public class TimeSlotServiceImpl extends ServiceImpl<TimeSlotMapper, TimeSlot> implements TimeSlotService {
}

