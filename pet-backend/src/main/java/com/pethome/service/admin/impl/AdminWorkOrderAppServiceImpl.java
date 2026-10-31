package com.pethome.service.admin.impl;

import com.pethome.common.AdminRole;
import com.pethome.common.BusinessException;
import com.pethome.common.ServiceTypeCatalog;
import com.pethome.entity.Admin;
import com.pethome.entity.Pet;
import com.pethome.entity.ServiceAppointmentRating;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.User;
import com.pethome.service.admin.AdminService;
import com.pethome.service.admin.AdminWorkOrderAppService;
import com.pethome.service.appointment.DoorCleaningAppointmentService;
import com.pethome.service.appointment.GroomingAppointmentService;
import com.pethome.service.appointment.HospitalAppointmentService;
import com.pethome.service.appointment.ServiceAppointmentRatingService;
import com.pethome.service.appointment.ServiceMemberService;
import com.pethome.service.user.PetService;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminWorkOrderAppServiceImpl implements AdminWorkOrderAppService {

    private static final Logger log = LoggerFactory.getLogger(AdminWorkOrderAppServiceImpl.class);

    @Autowired
    private ServiceMemberService serviceMemberService;

    @Autowired
    private PetService petService;

    @Autowired
    private AdminService adminService;

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private ServiceAppointmentRatingService serviceAppointmentRatingService;

    @Autowired(required = false)
    private DoorCleaningAppointmentService doorCleaningAppointmentService;

    @Autowired(required = false)
    private GroomingAppointmentService groomingAppointmentService;

    @Autowired(required = false)
    private HospitalAppointmentService hospitalAppointmentService;

    @Override
    public List<Map<String, Object>> getMyWorkOrders(Long adminId) {
        requireLogin(adminId);
        ServiceMember member = serviceMemberService.getMemberByUserId(adminId);
        if (member == null) {
            return Collections.emptyList();
        }
        Long memberId = member.getId();
        List<Map<String, Object>> orders = new ArrayList<>();

        if (doorCleaningAppointmentService != null) {
            List<com.pethome.entity.DoorCleaningAppointment> list =
                    doorCleaningAppointmentService.getAppointmentsByMemberId(memberId);
            if (list != null) {
                for (com.pethome.entity.DoorCleaningAppointment apt : list) {
                    orders.add(toWorkOrderMap(ServiceTypeCatalog.DOOR_CLEANING, apt.getServiceName(), apt.getId(),
                            apt.getContactName(), apt.getContactPhone(), apt.getDate(), apt.getAppointmentDate(),
                            apt.getTimeSlot(), apt.getStatus(), apt.getPetId(), apt.getRemark(), apt.getLocation()));
                }
            }
        }
        if (groomingAppointmentService != null) {
            List<com.pethome.entity.GroomingAppointment> list =
                    groomingAppointmentService.getAppointmentsByMemberId(memberId);
            if (list != null) {
                for (com.pethome.entity.GroomingAppointment apt : list) {
                    orders.add(toWorkOrderMap(ServiceTypeCatalog.GROOMING, apt.getServiceName(), apt.getId(),
                            apt.getContactName(), apt.getContactPhone(), apt.getDate(), null,
                            apt.getTimeSlot(), apt.getStatus(), apt.getPetId(), apt.getRemark(), apt.getLocation()));
                }
            }
        }
        if (hospitalAppointmentService != null) {
            List<com.pethome.entity.HospitalAppointment> list =
                    hospitalAppointmentService.getAppointmentsByMemberId(memberId);
            if (list != null) {
                for (com.pethome.entity.HospitalAppointment apt : list) {
                    orders.add(toWorkOrderMap(ServiceTypeCatalog.HOSPITAL, apt.getServiceName(), apt.getId(),
                            apt.getContactName(), apt.getContactPhone(), apt.getDate(), null,
                            apt.getTimeSlot(), apt.getStatus(), apt.getPetId(), apt.getRemark(), apt.getLocation()));
                }
            }
        }

        orders.sort((a, b) -> {
            int cmp = String.valueOf(b.get("date")).compareTo(String.valueOf(a.get("date")));
            if (cmp != 0) {
                return cmp;
            }
            return String.valueOf(b.get("timeSlot")).compareTo(String.valueOf(a.get("timeSlot")));
        });
        return orders;
    }

    @Override
    public List<Map<String, Object>> getMyReviews(Long adminId) {
        requireLogin(adminId);
        if (serviceAppointmentRatingService == null) {
            return Collections.emptyList();
        }
        ServiceMember member = serviceMemberService.getMemberByUserId(adminId);
        if (member == null) {
            return Collections.emptyList();
        }
        List<ServiceAppointmentRating> ratings = serviceAppointmentRatingService.listByMemberId(member.getId());
        if (ratings == null || ratings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (ServiceAppointmentRating rating : ratings) {
            String appointmentType = rating.getAppointmentType();
            Long userId = rating.getUserId();
            Map<String, Object> item = new HashMap<>();
            item.put("id", rating.getId());
            item.put("appointmentType", appointmentType);
            item.put("appointmentTypeName", ServiceTypeCatalog.typeName(appointmentType));
            item.put("appointmentId", rating.getAppointmentId());
            item.put("rating", rating.getRating());
            item.put("comment", rating.getComment());
            item.put("createTime", rating.getCreateTime());
            item.put("userName", resolveReviewUserName(userId));
            result.add(item);
        }
        return result;
    }

    @Override
    public String updateMyWorkOrderStatus(Long adminId, Map<String, Object> body) {
        requireLogin(adminId);
        ServiceMember member = serviceMemberService.getMemberByUserId(adminId);
        if (member == null) {
            throw new BusinessException("您尚未分配服务部门，无法操作工单");
        }
        if (body == null) {
            throw new BusinessException("参数不完整");
        }
        Long memberId = member.getId();
        Object idObj = body.get("id");
        Object serviceTypeObj = body.get("serviceType");
        Object statusObj = body.get("status");
        if (idObj == null || serviceTypeObj == null || statusObj == null) {
            throw new BusinessException("参数不完整");
        }
        Long orderId = parseOrderId(idObj);
        String serviceType = serviceTypeObj.toString();
        String status = statusObj.toString();
        validateOperableStatus(status);
        String rejectReason = resolveRejectReason(body, status);

        String updatedType = tryUpdateOwnedByMember(serviceType, orderId, memberId, status, rejectReason);
        if (updatedType != null) {
            return "状态更新成功";
        }
        throw new BusinessException("工单不存在或您无权操作");
    }

    @Override
    public String updateWorkOrderStatus(Long adminId, Map<String, Object> body) {
        requireLogin(adminId);
        if (body == null) {
            throw new BusinessException("参数不完整");
        }
        Object idObj = body.get("id");
        Object statusObj = body.get("status");
        if (idObj == null || statusObj == null) {
            throw new BusinessException("参数不完整");
        }
        Long orderId = parseOrderId(idObj);
        String status = statusObj.toString().trim();
        validateOperableStatus(status);
        String rejectReason = resolveRejectReason(body, status);
        boolean forceCancel = "cancelled".equals(status);

        Admin operator = adminService != null ? adminService.getById(adminId) : null;
        String role = AdminRole.normalize(operator != null ? operator.getRole() : AdminRole.STAFF);
        boolean isStaff = AdminRole.STAFF.equals(role);
        boolean isStoreAdmin = AdminRole.STORE_ADMIN.equals(role);
        Long operatorStoreId = resolveOperatorStoreId(operator);
        Long staffMemberId = null;
        if (isStaff) {
            ServiceMember member = serviceMemberService.getMemberByUserId(adminId);
            if (member == null) {
                throw new BusinessException("您尚未分配服务部门，无法操作工单");
            }
            staffMemberId = member.getId();
        }

        for (String type : buildServiceTypeCandidates(body.get("serviceType"))) {
            if (tryUpdateWorkOrderStatus(type, orderId, status, rejectReason, forceCancel,
                    isStaff, staffMemberId, isStoreAdmin, operatorStoreId)) {
                return "状态更新成功";
            }
        }
        throw new BusinessException("状态更新失败，记录不存在");
    }

    @Override
    public Map<String, Object> getStaffAppointments(Long adminId) {
        ServiceMember member = serviceMemberService.getMemberByUserId(adminId);
        if (member == null) {
            return Map.of("projects", List.of(), "tasks", List.of());
        }
        Long memberId = member.getId();
        List<Map<String, Object>> projects = new ArrayList<>();
        List<Map<String, Object>> tasks = new ArrayList<>();
        int completedCount = 0;
        int totalCount = 0;

        if (doorCleaningAppointmentService != null) {
            List<com.pethome.entity.DoorCleaningAppointment> list =
                    doorCleaningAppointmentService.getAppointmentsByMemberId(memberId);
            if (list != null) {
                for (com.pethome.entity.DoorCleaningAppointment apt : list) {
                    totalCount++;
                    if ("completed".equals(apt.getStatus())) {
                        completedCount++;
                    }
                    tasks.add(toTaskMap(ServiceTypeCatalog.DOOR_CLEANING, apt.getId(), apt.getDate(),
                            apt.getTimeSlot(), apt.getStatus(), apt.getContactName()));
                }
            }
        }
        if (groomingAppointmentService != null) {
            List<com.pethome.entity.GroomingAppointment> list =
                    groomingAppointmentService.getAppointmentsByMemberId(memberId);
            if (list != null) {
                for (com.pethome.entity.GroomingAppointment apt : list) {
                    totalCount++;
                    if ("completed".equals(apt.getStatus())) {
                        completedCount++;
                    }
                    tasks.add(toTaskMap(ServiceTypeCatalog.GROOMING, apt.getId(), apt.getDate(),
                            apt.getTimeSlot(), apt.getStatus(), apt.getContactName()));
                }
            }
        }
        if (hospitalAppointmentService != null) {
            List<com.pethome.entity.HospitalAppointment> list =
                    hospitalAppointmentService.getAppointmentsByMemberId(memberId);
            if (list != null) {
                for (com.pethome.entity.HospitalAppointment apt : list) {
                    totalCount++;
                    if ("completed".equals(apt.getStatus())) {
                        completedCount++;
                    }
                    tasks.add(toTaskMap(ServiceTypeCatalog.HOSPITAL, apt.getId(), apt.getDate(),
                            apt.getTimeSlot(), apt.getStatus(), apt.getContactName()));
                }
            }
        }

        projects.add(Map.<String, Object>of(
                "name", ServiceTypeCatalog.departmentName(member.getServiceType()),
                "role", "服务人员",
                "progress", totalCount > 0 ? (int) (100.0 * completedCount / totalCount) : 0,
                "totalCount", totalCount,
                "completedCount", completedCount));

        tasks.sort((a, b) -> {
            int cmp = String.valueOf(b.get("date")).compareTo(String.valueOf(a.get("date")));
            return cmp != 0 ? cmp : String.valueOf(b.get("timeSlot")).compareTo(String.valueOf(a.get("timeSlot")));
        });
        return Map.of("projects", projects, "tasks", tasks);
    }

    private void requireLogin(Long adminId) {
        if (adminId == null) {
            throw new BusinessException("未登录或Token无效");
        }
    }

    private Long parseOrderId(Object idObj) {
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        try {
            return Long.parseLong(idObj.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException("工单ID格式不正确");
        }
    }

    private void validateOperableStatus(String status) {
        if (!"confirmed".equals(status) && !"completed".equals(status) && !"cancelled".equals(status)) {
            throw new BusinessException("无效的状态值");
        }
    }

    private String resolveRejectReason(Map<String, Object> body, String status) {
        if (!"cancelled".equals(status)) {
            return null;
        }
        Object reasonObj = body.get("rejectReason");
        if (reasonObj == null || reasonObj.toString().trim().isEmpty()) {
            return null;
        }
        return reasonObj.toString().trim();
    }

    /** 员工端：仅允许操作分配给自己的工单，命中返回服务类型，否则返回 null */
    private String tryUpdateOwnedByMember(String serviceType, Long orderId, Long memberId,
                                          String status, String rejectReason) {
        boolean forceCancel = "cancelled".equals(status);
        if (ServiceTypeCatalog.DOOR_CLEANING.equals(serviceType) && doorCleaningAppointmentService != null) {
            var apt = doorCleaningAppointmentService.getAppointmentById(orderId);
            if (apt != null && memberId.equals(apt.getMemberId())
                    && doorCleaningAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel)) {
                return serviceType;
            }
            return null;
        }
        if (ServiceTypeCatalog.GROOMING.equals(serviceType) && groomingAppointmentService != null) {
            var apt = groomingAppointmentService.getAppointmentById(orderId);
            if (apt != null && memberId.equals(apt.getMemberId())
                    && groomingAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel)) {
                return serviceType;
            }
            return null;
        }
        if (ServiceTypeCatalog.HOSPITAL.equals(serviceType) && hospitalAppointmentService != null) {
            var apt = hospitalAppointmentService.getAppointmentById(orderId);
            if (apt != null && memberId.equals(apt.getMemberId())
                    && hospitalAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel)) {
                return serviceType;
            }
            return null;
        }
        return null;
    }

    /** 管理端：按角色做数据隔离后更新，成功返回 true */
    private boolean tryUpdateWorkOrderStatus(String serviceType, Long orderId, String status, String rejectReason,
                                             boolean forceCancel, boolean isStaff, Long staffMemberId,
                                             boolean isStoreAdmin, Long operatorStoreId) {
        if (ServiceTypeCatalog.DOOR_CLEANING.equals(serviceType) && doorCleaningAppointmentService != null) {
            var apt = doorCleaningAppointmentService.getAppointmentById(orderId);
            if (apt == null) {
                return false;
            }
            if (isStaff && (apt.getMemberId() == null || !apt.getMemberId().equals(staffMemberId))) {
                return false;
            }
            if (isStoreAdmin && !canStoreAdminOperate(apt.getStoreId(), operatorStoreId)) {
                return false;
            }
            return doorCleaningAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
        }
        if (ServiceTypeCatalog.GROOMING.equals(serviceType) && groomingAppointmentService != null) {
            var apt = groomingAppointmentService.getAppointmentById(orderId);
            if (apt == null) {
                return false;
            }
            if (isStaff && (apt.getMemberId() == null || !apt.getMemberId().equals(staffMemberId))) {
                return false;
            }
            if (isStoreAdmin && !canStoreAdminOperate(apt.getStoreId(), operatorStoreId)) {
                return false;
            }
            return groomingAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
        }
        if (ServiceTypeCatalog.HOSPITAL.equals(serviceType) && hospitalAppointmentService != null) {
            var apt = hospitalAppointmentService.getAppointmentById(orderId);
            if (apt == null) {
                return false;
            }
            if (isStaff && (apt.getMemberId() == null || !apt.getMemberId().equals(staffMemberId))) {
                return false;
            }
            if (isStoreAdmin && !canStoreAdminOperate(apt.getStoreId(), operatorStoreId)) {
                return false;
            }
            return hospitalAppointmentService.updateAppointmentStatus(orderId, status, rejectReason, forceCancel);
        }
        return false;
    }

    private List<String> buildServiceTypeCandidates(Object serviceTypeObj) {
        String normalized = serviceTypeObj == null ? "" : serviceTypeObj.toString().trim().toLowerCase();
        if (ServiceTypeCatalog.DOOR_CLEANING.equals(normalized) || "litter".equals(normalized)) {
            return Arrays.asList(ServiceTypeCatalog.DOOR_CLEANING, ServiceTypeCatalog.GROOMING, ServiceTypeCatalog.HOSPITAL);
        }
        if (ServiceTypeCatalog.GROOMING.equals(normalized)) {
            return Arrays.asList(ServiceTypeCatalog.GROOMING, ServiceTypeCatalog.DOOR_CLEANING, ServiceTypeCatalog.HOSPITAL);
        }
        if (ServiceTypeCatalog.HOSPITAL.equals(normalized)) {
            return Arrays.asList(ServiceTypeCatalog.HOSPITAL, ServiceTypeCatalog.DOOR_CLEANING, ServiceTypeCatalog.GROOMING);
        }
        return Arrays.asList(ServiceTypeCatalog.DOOR_CLEANING, ServiceTypeCatalog.GROOMING, ServiceTypeCatalog.HOSPITAL);
    }

    private Long resolveOperatorStoreId(Admin operator) {
        if (operator == null) {
            return null;
        }
        if (operator.getServiceStoreId() != null) {
            return operator.getServiceStoreId();
        }
        return operator.getStoreId();
    }

    private boolean canStoreAdminOperate(Long appointmentStoreId, Long operatorStoreId) {
        return operatorStoreId != null && appointmentStoreId != null && appointmentStoreId.equals(operatorStoreId);
    }

    private String resolveReviewUserName(Long userId) {
        if (userId == null || userService == null) {
            return "匿名用户";
        }
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return "用户" + userId;
            }
            if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                return user.getNickname();
            }
            return user.getUsername();
        } catch (Exception e) {
            log.warn("查询评价用户信息失败: userId={}", userId, e);
            return "用户" + userId;
        }
    }

    private Map<String, Object> toWorkOrderMap(String type, String serviceProjectName, Object id,
                                               String contactName, String contactPhone, Object date, Object appointmentDate,
                                               String timeSlot, String status, Object petId, String remark, String address) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("serviceType", type);
        m.put("serviceTypeName", ServiceTypeCatalog.typeName(type));
        m.put("serviceProjectName", serviceProjectName != null && !serviceProjectName.trim().isEmpty()
                ? serviceProjectName.trim() : null);
        m.put("contactName", contactName != null ? contactName : "-");
        m.put("contactPhone", contactPhone != null ? contactPhone : "-");
        Object d = date != null ? date : appointmentDate;
        if (d != null) {
            String s = d.toString();
            m.put("date", s.length() > 10 && s.contains("T") ? s.substring(0, 10) : s);
        } else {
            m.put("date", "");
        }
        m.put("timeSlot", timeSlot != null ? timeSlot : "-");
        m.put("status", status != null ? status : "pending");
        m.put("petId", petId);
        if (petId != null) {
            putPetInfo(m, petId);
        } else {
            m.put("petName", null);
        }
        m.put("remark", remark != null && !remark.trim().isEmpty() ? remark.trim() : null);
        m.put("address", address != null && !address.trim().isEmpty() ? address.trim() : null);
        return m;
    }

    private void putPetInfo(Map<String, Object> target, Object petId) {
        try {
            Long pid = Long.valueOf(petId.toString());
            Pet pet = petService.getPetById(pid);
            if (pet == null) {
                target.put("petName", "宠物已删除");
                return;
            }
            target.put("petName", pet.getName() != null ? pet.getName() : "未命名");
            target.put("petBreed", pet.getBreed());
            target.put("petGender", pet.getGender());
            target.put("petAge", pet.getAge());
            target.put("petWeight", pet.getWeight());
            target.put("petSpecies", pet.getSpecies());
            target.put("petAvatar", pet.getAvatar());
        } catch (Exception e) {
            log.warn("查询宠物信息失败: petId={}", petId, e);
            target.put("petName", "未知宠物");
        }
    }

    private Map<String, Object> toTaskMap(String type, Object id, Object date, Object timeSlot,
                                          Object status, Object contact) {
        Map<String, Object> m = new HashMap<>();
        String typeName = ServiceTypeCatalog.typeName(type);
        m.put("id", id);
        m.put("project", typeName);
        m.put("title", (contact != null ? contact : "") + " - " + typeName);
        m.put("date", date != null ? date.toString() : "");
        m.put("timeSlot", timeSlot != null ? timeSlot : "");
        m.put("dueDate", date != null ? date.toString() : "");
        m.put("status", status != null ? status : "");
        return m;
    }
}
