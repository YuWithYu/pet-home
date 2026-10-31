package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Booking;
import com.pethome.mapper.BookingMapper;
import com.pethome.service.appointment.BookingService;
import org.springframework.stereotype.Service;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {
}

