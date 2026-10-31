package com.pethome.controller.user;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.entity.Pet;
import com.pethome.entity.PetWeightRecord;
import com.pethome.service.user.PetService;
import com.pethome.service.user.PetWeightRecordService;
import com.pethome.security.UserTokenResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 宠物体重记录：健康档案体重曲线数据源
 *
 * 安全约束：所有读写均通过 token 解析当前用户并校验宠物归属，
 * 不接受 query/body 传入的 userId。
 */
@RestController
@RequestMapping("/api/pet-weights")
@Tag(name = "宠物体重记录")
public class PetWeightRecordController {

    @Autowired
    private PetWeightRecordService weightRecordService;

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

    /** 校验宠物归属当前用户 */
    private void requireOwnedPet(Integer petId, Long userId) {
        if (petId == null) {
            throw new BusinessException("宠物ID不能为空");
        }
        Pet pet = petService.getPetById(petId.longValue());
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        if (userId == null || pet.getUserId() == null
                || !userId.equals(pet.getUserId().longValue())) {
            throw new BusinessException("无权操作该宠物的体重记录");
        }
    }

    /** 校验记录归属当前用户（通过宠物间接校验） */
    private PetWeightRecord requireOwnedRecord(Long recordId, Long userId) {
        PetWeightRecord record = weightRecordService.getById(recordId);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }
        requireOwnedPet(record.getPetId(), userId);
        return record;
    }

    @GetMapping("/pet/{petId}")
    @Operation(summary = "查询宠物体重记录（按日期正序）")
    public Result<List<PetWeightRecord>> listByPet(
            @PathVariable Integer petId,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        requireOwnedPet(petId, userId);
        return Result.success(weightRecordService.listByPetId(petId));
    }

    @PostMapping("/create")
    @Operation(summary = "记录体重（同一天重复记录会覆盖）")
    public Result<PetWeightRecord> create(
            @RequestBody PetWeightRecord body,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        requireOwnedPet(body.getPetId(), userId);
        if (body.getWeight() == null
                || body.getWeight().doubleValue() <= 0
                || body.getWeight().doubleValue() > 200) {
            throw new BusinessException("请输入有效的体重（0~200kg）");
        }
        if (body.getRecordDate() == null) {
            throw new BusinessException("称重日期不能为空");
        }
        body.setUserId(userId);
        body.setId(null);
        return Result.success(weightRecordService.add(body));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除体重记录")
    public Result<Void> delete(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        requireOwnedRecord(id, userId);
        weightRecordService.deleteById(id);
        return Result.success(null);
    }

    @PutMapping("/update")
    @Operation(summary = "更新体重记录")
    public Result<PetWeightRecord> update(
            @RequestBody PetWeightRecord body,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = resolveOwnerId(request, authHeader);
        if (body.getId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        // 归属校验：记录必须属于当前用户
        PetWeightRecord owned = requireOwnedRecord(body.getId(), userId);
        if (body.getWeight() != null
                && (body.getWeight().doubleValue() <= 0 || body.getWeight().doubleValue() > 200)) {
            throw new BusinessException("请输入有效的体重（0~200kg）");
        }
        // 不允许修改归属字段与关联宠物（用数据库现有值覆盖 body 中任何篡改）
        body.setUserId(userId);
        body.setPetId(owned.getPetId());
        return Result.success(weightRecordService.update(body));
    }
}
