package com.pethome.service.admin;

import com.pethome.entity.Admin;

import java.util.List;

/**
 * 后台账号（员工/商家）管理应用服务。
 *
 * <p>负责账号列表的数据隔离、评分/工作量聚合、账号增删改的角色校验与唯一管理员保护。</p>
 */
public interface AdminStaffAppService {

    /**
     * 查询员工/商家账号列表。
     *
     * @param department    部门筛选（可为空）
     * @param storeId       门店筛选（可为空，门店管理员会被强制收敛到本店）
     * @param accountsOnly  仅账号管理页使用的开关
     * @param currentAdminId 当前登录账号，未登录为空
     */
    List<Admin> listStaff(String department, Long storeId, Boolean accountsOnly, Long currentAdminId);

    /** 新增员工/商家账号，仅管理员可操作 */
    Admin addStaff(Admin staff, String currentRole);

    /** 更新员工/商家账号，含自我保护与唯一管理员保护 */
    boolean updateStaff(Admin staff, Long currentAdminId);

    /** 删除员工/商家账号，含未完成预约校验与唯一管理员保护 */
    boolean deleteStaff(Long id, Long currentAdminId);

    /** 员工详情（含评分、任务量等展示字段） */
    Admin getStaffById(Long id);
}
