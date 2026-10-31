package com.pethome.controller.user;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.entity.Pet;
import com.pethome.entity.PetMedicalRecord;
import com.pethome.service.user.PetMedicalRecordService;
import com.pethome.service.user.PetService;
import com.pethome.security.UserTokenResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宠物健康档案：就医记录（疫苗/驱虫/体检/诊疗）与到期提醒
 *
 * 安全约束：所有读写均通过 token 解析当前用户并校验宠物归属，
 * 不接受 query/body 传入的 userId。
 */
@RestController
@RequestMapping("/api/pet-medical-records")
@Tag(name = "宠物健康档案")
public class PetMedicalRecordController {

    @Autowired
    private PetMedicalRecordService medicalRecordService;

    @Autowired
    private PetService petService;

    @Autowired
    private UserTokenResolver userTokenResolver;

    /** 从 Bearer 解析当前用户 ID（统一走 UserTokenResolver） */
    private Long resolveCurrentUserId(String authHeader) {
        return userTokenResolver.getUserIdFromBearer(authHeader);
    }

    /** 优先使用拦截器写入的 userId，否则从 Authorization 解析 */
    private Long resolveOwnerId(HttpServletRequest request, String authHeader) {
        if (request != null) {
            Object attr = request.getAttribute("userId");
            if (attr instanceof Number) {
                return ((Number) attr).longValue();
            }
        }
        return resolveCurrentUserId(authHeader);
    }

    /** 校验宠物归属当前用户，返回宠物实体 */
    private Pet requireOwnedPet(Integer petId, Long userId) {
        if (petId == null) {
            throw new BusinessException("宠物ID不能为空");
        }
        Pet pet = petService.getPetById(petId.longValue());
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        if (userId == null || pet.getUserId() == null
                || !userId.equals(pet.getUserId().longValue())) {
            throw new BusinessException("无权操作该宠物的健康档案");
        }
        return pet;
    }

    /** 校验记录归属当前用户（通过宠物间接校验），返回记录实体 */
    private PetMedicalRecord requireOwnedRecord(Long recordId, Long userId) {
        PetMedicalRecord record = medicalRecordService.getById(recordId);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }
        requireOwnedPet(record.getPetId(), userId);
        return record;
    }

    @GetMapping("/pet/{petId}")
    @Operation(summary = "查询宠物的就医记录（时间倒序）")
    public Result<List<PetMedicalRecord>> listByPet(
            @PathVariable Integer petId,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        requireOwnedPet(petId, userId);
        return Result.success(medicalRecordService.listByPetId(petId));
    }

    @GetMapping("/pet/{petId}/upcoming")
    @Operation(summary = "查询即将到期/已过期的疫苗驱虫提醒（默认30天窗口）")
    public Result<List<Map<String, Object>>> upcoming(
            @PathVariable Integer petId,
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        requireOwnedPet(petId, userId);
        List<Map<String, Object>> list = medicalRecordService
                .listUpcoming(petId, days == null ? 30 : Math.min(days, 365))
                .stream().map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", r.getId());
                    m.put("petId", r.getPetId());
                    m.put("recordType", r.getRecordType());
                    m.put("typeText", medicalRecordService.typeText(r.getRecordType()));
                    m.put("itemName", r.getItemName());
                    m.put("recordDate", r.getRecordDate());
                    m.put("nextDueDate", r.getNextDueDate());
                    m.put("daysLeft", medicalRecordService.daysUntil(r.getNextDueDate()));
                    return m;
                }).toList();
        return Result.success(list);
    }

    @PostMapping("/create")
    @Operation(summary = "新增就医记录")
    public Result<PetMedicalRecord> create(
            @RequestBody PetMedicalRecord body,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        requireOwnedPet(body.getPetId(), userId);
        if (body.getRecordType() == null || body.getRecordType().isBlank()) {
            throw new BusinessException("记录类型不能为空");
        }
        if (body.getItemName() == null || body.getItemName().isBlank()) {
            throw new BusinessException("项目名称不能为空");
        }
        if (body.getRecordDate() == null) {
            throw new BusinessException("就诊/接种日期不能为空");
        }
        body.setUserId(userId);
        body.setId(null);
        return Result.success(medicalRecordService.add(body));
    }

    @PutMapping("/update")
    @Operation(summary = "更新就医记录")
    public Result<PetMedicalRecord> update(
            @RequestBody PetMedicalRecord body,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        if (body.getId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        requireOwnedRecord(body.getId(), userId);
        // 归属字段不允许被篡改
        body.setUserId(userId);
        body.setHospitalAppointmentId(null);
        return Result.success(medicalRecordService.update(body));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除就医记录")
    public Result<Void> delete(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        requireOwnedRecord(id, userId);
        medicalRecordService.deleteById(id);
        return Result.success(null);
    }
}
