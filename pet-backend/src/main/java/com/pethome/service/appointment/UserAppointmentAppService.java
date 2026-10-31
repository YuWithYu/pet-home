package com.pethome.service.appointment;

import java.util.List;
import java.util.Map;

/**
 * 用户端「我的预约」聚合应用服务：一次返回上门铲屎、医院、洗护三类预约
 */
public interface UserAppointmentAppService {

    /**
     * 聚合指定用户的全部预约，按创建时间倒序；单类查询失败时降级为空列表，不影响其他类型
     */
    List<Map<String, Object>> listUserAppointments(Long userId);
}
