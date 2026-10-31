package com.pethome.service.admin;

import java.util.List;
import java.util.Map;

/**
 * 后台工单查询与状态流转应用服务。
 *
 * <p>覆盖三类预约（上门铲屎、宠物洗护、宠物医院）在管理端/员工端的统一视图与操作，
 * 表现层只负责解析身份并把结果包装成统一响应。</p>
 */
public interface AdminWorkOrderAppService {

    /** 员工端：当前账号被分配的工单列表 */
    List<Map<String, Object>> getMyWorkOrders(Long adminId);

    /** 员工端：当前账号收到的真实评价 */
    List<Map<String, Object>> getMyReviews(Long adminId);

    /** 员工端：更新自己名下的工单状态 */
    String updateMyWorkOrderStatus(Long adminId, Map<String, Object> body);

    /** 管理端/员工端统一更新工单状态（按角色做数据隔离） */
    String updateWorkOrderStatus(Long adminId, Map<String, Object> body);

    /** 团队管理：某员工的预约任务与项目参与情况 */
    Map<String, Object> getStaffAppointments(Long adminId);
}
