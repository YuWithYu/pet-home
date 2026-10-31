package com.pethome.service.appointment;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.Booking;

import java.util.List;
import java.util.Map;

/**
 * 通用预约（不含上门铲屎/洗护/医院三类专项预约）应用服务。
 * <p>
 * 表现层只负责解析登录身份，时间段容量统计、创建与取消的状态校验都集中在此，
 * 避免各 Controller 自行拼装 QueryWrapper 或重复判断。
 */
public interface BookingService extends IService<Booking> {

    /** 指定服务类型与日期的可用时间段（当天已结束的时段会被过滤） */
    List<Map<String, Object>> getAvailableTimeSlots(String serviceType, String date);

    /** 创建预约：校验时间段是否存在与是否已满，归属以当前登录用户为准 */
    Booking createBooking(Booking booking, Long userId);

    /** 当前用户的预约分页 */
    IPage<Booking> getUserBookingPage(Long userId, Integer current, Integer size, String status, String contactPhone);

    /** 取消预约：仅本人可取消，已取消/已完成/已失约的单据不可取消 */
    Booking cancelBooking(Long bookingId, Long userId);
}
