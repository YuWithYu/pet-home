package com.pethome.controller.user;


import com.pethome.common.BusinessException;
import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Pet;
import com.pethome.service.user.PetService;
import com.pethome.security.UserTokenResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "宠物管理")
public class PetController {

    private static final Logger logger = LoggerFactory.getLogger(PetController.class);

    @Autowired
    private PetService petService;

    @Autowired
    private UserTokenResolver userTokenResolver;

    /**
     * 从 Bearer 解析当前用户 ID（统一走 UserTokenResolver）
     */
    private Long resolveCurrentUserId(String authHeader) {
        return userTokenResolver.getUserIdFromBearer(authHeader);
    }

    /**
     * 优先使用拦截器写入的 userId（与 Redis/JWT 校验一致），否则从 Authorization 解析。
     */
    private Long resolveOwnerId(HttpServletRequest request, String authHeader) {
        if (request != null) {
            Object attr = request.getAttribute("userId");
            if (attr instanceof Long) {
                return (Long) attr;
            }
            if (attr instanceof Integer) {
                return ((Integer) attr).longValue();
            }
            if (attr instanceof Number) {
                return ((Number) attr).longValue();
            }
        }
        return resolveCurrentUserId(authHeader);
    }

    /** 管理后台等：带 adminId 或运营角色时可查全库 */
    private boolean isAdminOrStaff(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        if (request.getAttribute("adminId") != null) {
            return true;
        }
        Object roleObj = request.getAttribute("role");
        if (!(roleObj instanceof String)) {
            return false;
        }
        String r = ((String) roleObj).toLowerCase();
        return "super_admin".equals(r) || "admin".equals(r) || "staff".equals(r);
    }

    private Page<Pet> emptyPetPage(int pageNo, int pageSize) {
        Page<Pet> p = new Page<>(pageNo, pageSize, 0);
        p.setRecords(List.of());
        return p;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询宠物")
    public Result<IPage<Pet>> getPetPage(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        int pn = page != null ? page : (pageNo != null ? pageNo : 1);
        int ps = size != null ? size : (pageSize != null ? pageSize : 10);
        if (pn < 1) {
            pn = 1;
        }
        if (ps < 1) {
            ps = 10;
        }

        logger.info("========== 获取宠物列表 ==========");
        logger.info("page: {}, size: {}, userId: {}", pn, ps, userId);

        boolean admin = isAdminOrStaff(request);
        Long effectiveUserId = userId;

        if (userId != null) {
            // 安全修复：原实现直接信任查询参数 userId，任意登录用户可枚举他人宠物档案。
            // 非管理端角色必须以 token 身份为准，请求参数与 token 不一致时拒绝。
            if (!admin) {
                Long ownerId = resolveOwnerId(request, authHeader);
                if (ownerId == null || !ownerId.equals(userId)) {
                    logger.warn("宠物列表越权尝试：请求 userId={}, 当前登录 userId={}", userId, ownerId);
                    throw new BusinessException(403, "无权查看他人宠物");
                }
                effectiveUserId = ownerId;
            }
        } else {
            if (admin) {
                effectiveUserId = null;
            } else {
                Long ownerId = resolveOwnerId(request, authHeader);
                if (ownerId == null) {
                    logger.info("未登录且未传 userId：不再返回全库，返回空列表");
                    return Result.success(emptyPetPage(pn, ps));
                }
                effectiveUserId = ownerId;
            }
        }

        // 缓存优先、失败降级数据库分页的兜底逻辑放在 Service 层
        IPage<Pet> result = petService.getPetPageForOwner(effectiveUserId, pn, ps);

        logger.info("查询结果 - 总数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());
        if (!result.getRecords().isEmpty()) {
            logger.info("第一条记录: {}", result.getRecords().get(0).toString());
        }
        logger.info("====================================");

        return Result.success(result);
    }

    @PostMapping("/create")
    @Operation(summary = "创建宠物")
    public Result<Pet> createPet(
            @RequestBody Pet pet,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return com.pethome.common.SafeCall.of(() -> {
            logger.info("========== 创建宠物 ==========");
            if (pet == null) {
                throw new BusinessException(400, "请求体不能为空");
            }
            logger.info("接收到的宠物数据: {}", pet.toString());

            Long ownerId = resolveOwnerId(request, authHeader);
            if (ownerId == null) {
                throw new BusinessException(401, "请先登录后再添加宠物");
            }
            pet.setUserId(ownerId);
            logger.info("归属用户 userId={}", ownerId);
            
            // 字段映射：前端可能发送type，需要映射到species
            if (pet.getSpecies() == null || pet.getSpecies().isEmpty()) {
                // 可以通过反射或其他方式获取type字段，这里先简化处理
                logger.warn("警告: species字段为空");
            }
            
            // 设置默认值
            if (pet.getStatus() == null || pet.getStatus().isEmpty()) {
                pet.setStatus("active");
            }

            Pet createdPet = petService.createPet(pet);
            logger.info("宠物创建成功，ID: {}, 已自动清除缓存", createdPet.getId());
            
            return Result.success(createdPet);
        }, "创建宠物失败，请稍后重试", logger);
    }

    @PutMapping("/update")
    @Operation(summary = "更新宠物")
    public Result<Pet> updatePet(
            @RequestBody Pet pet,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return com.pethome.common.SafeCall.of(() -> {
            logger.info("========== 更新宠物 ==========");
            if (pet == null || pet.getId() == null) {
                throw new BusinessException(400, "宠物ID不能为空");
            }
            Long ownerId = resolveOwnerId(request, authHeader);
            if (ownerId == null) {
                throw new BusinessException(401, "请先登录");
            }
            Pet existing = petService.getPetById(pet.getId().longValue());
            if (existing == null) {
                throw new BusinessException(404, "宠物不存在");
            }
            if (existing.getUserId() == null) {
                throw new BusinessException(400, "宠物数据异常");
            }
            if (!ownerId.equals(existing.getUserId().longValue())) {
                throw new BusinessException(403, "无权修改该宠物");
            }
            pet.setUserId(existing.getUserId());

            logger.info("宠物ID: {}, userId: {}", pet.getId(), pet.getUserId());

            Pet updatedPet = petService.updatePet(pet);
            logger.info("宠物更新成功，ID: {}, 已自动清除缓存", updatedPet.getId());
            
            return Result.success(updatedPet);
        }, "更新宠物失败，请稍后重试", logger);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物")
    public Result<Boolean> deletePet(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return com.pethome.common.SafeCall.of(() -> {
            logger.info("========== 删除宠物 ==========");
            logger.info("宠物ID: {}", id);

            Long ownerId = resolveOwnerId(request, authHeader);
            if (ownerId == null) {
                throw new BusinessException(401, "请先登录");
            }
            Pet existing = petService.getPetById(id);
            if (existing == null) {
                throw new BusinessException(404, "宠物不存在");
            }
            if (existing.getUserId() == null) {
                throw new BusinessException(400, "宠物数据异常");
            }
            if (!ownerId.equals(existing.getUserId().longValue())) {
                throw new BusinessException(403, "无权删除该宠物");
            }

            boolean result = petService.deletePet(id);
            if (result) {
                logger.info("宠物删除成功，ID: {}, 已自动清除缓存", id);
            } else {
                logger.warn("宠物删除失败，ID: {}", id);
            }
            
            return Result.success(result);
        }, "删除宠物失败，请稍后重试", logger);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物详情")
    public Result<Pet> getPetDetail(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return com.pethome.common.SafeCall.of(() -> {
            Pet pet = petService.getPetById(id);
            if (pet == null) {
                logger.warn("未找到宠物，ID: {}", id);
                throw new BusinessException(404, "宠物不存在");
            }

            // 安全修复（上次审查遗留项）：归属校验——管理端可查看任意宠物；
            // 普通用户仅可查看自己的宠物，防止匿名/越权枚举他人宠物及主人关联。
            // 该接口在 RoleInterceptor 匿名白名单中放行了 GET /api/pets/{id}，必须在 Controller 层把关。
            if (isAdminOrStaff(request)) {
                return Result.success(pet);
            }
            Long ownerId = resolveOwnerId(request, authHeader);
            if (ownerId == null || pet.getUserId() == null || !ownerId.equals(pet.getUserId().longValue())) {
                logger.warn("宠物详情归属校验未通过: petId={}, ownerId={}", id, ownerId);
                throw new BusinessException(403, "无权查看该宠物");
            }

            return Result.success(pet);
        }, "查询宠物详情失败，请稍后重试", logger);
    }
}
