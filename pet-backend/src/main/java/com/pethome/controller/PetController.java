package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Pet;
import com.pethome.entity.User;
import com.pethome.service.PetService;
import com.pethome.service.UserService;
import com.pethome.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
@Api(tags = "宠物管理")
public class PetController {

    private static final Logger logger = LoggerFactory.getLogger(PetController.class);

    @Autowired
    private PetService petService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从 Bearer 解析当前用户 ID（与 UserController 策略一致）
     */
    private Long resolveCurrentUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        User u = userService.getUserByToken(token);
        if (u != null && u.getId() != null) {
            return u.getId();
        }
        try {
            String subject = jwtUtil.getUsernameFromToken(token);
            if (subject != null) {
                User byPhone = userService.getUserByPhone(subject);
                if (byPhone != null) {
                    return byPhone.getId();
                }
                User byName = userService.getUserByUsername(subject);
                if (byName != null) {
                    return byName.getId();
                }
            }
        } catch (Exception e) {
            logger.debug("解析登录用户失败: {}", e.getMessage());
        }
        return null;
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
    @ApiOperation("分页查询宠物")
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

        if (userId == null) {
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

        // 如果指定了 effectiveUserId，优先使用缓存查询（不分页），然后手动分页
        if (effectiveUserId != null) {
            try {
                List<Pet> allPets = petService.getPetsByUserId(effectiveUserId);
                logger.info("从缓存/数据库获取到 {} 条宠物数据，开始分页处理", allPets.size());

                int total = allPets.size();
                int start = (pn - 1) * ps;
                int end = Math.min(start + ps, total);

                List<Pet> pagedList = start < total ? allPets.subList(start, end) : List.of();

                Page<Pet> pageResult = new Page<>(pn, ps, total);
                pageResult.setRecords(pagedList);

                logger.info("分页结果 - 总数: {}, 当前页记录数: {}", total, pagedList.size());
                logger.info("====================================");

                return Result.success(pageResult);
            } catch (Exception e) {
                logger.error("使用缓存查询失败，降级到数据库分页查询，错误: {}", e.getMessage(), e);
            }
        }

        Page<Pet> pageParam = new Page<>(pn, ps);
        IPage<Pet> result = petService.getPetListByUserId(pageParam, effectiveUserId);

        logger.info("查询结果 - 总数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());
        if (!result.getRecords().isEmpty()) {
            logger.info("第一条记录: {}", result.getRecords().get(0).toString());
        }
        logger.info("====================================");

        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物")
    public Result<Pet> createPet(
            @RequestBody Pet pet,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            logger.info("========== 创建宠物 ==========");
            if (pet == null) {
                return Result.error(400, "请求体不能为空");
            }
            logger.info("接收到的宠物数据: {}", pet.toString());

            Long ownerId = resolveOwnerId(request, authHeader);
            if (ownerId == null) {
                return Result.error(401, "请先登录后再添加宠物");
            }
            pet.setUserId(ownerId.intValue());
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
            logger.info("====================================");
            
            return Result.success(createdPet);
        } catch (Exception e) {
            logger.error("创建宠物失败: {}", e.getMessage(), e);
            return Result.error("创建宠物失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物")
    public Result<Pet> updatePet(
            @RequestBody Pet pet,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            logger.info("========== 更新宠物 ==========");
            if (pet == null || pet.getId() == null) {
                return Result.error(400, "宠物ID不能为空");
            }
            Long ownerId = resolveOwnerId(request, authHeader);
            if (ownerId == null) {
                return Result.error(401, "请先登录");
            }
            Pet existing = petService.getPetById(pet.getId().longValue());
            if (existing == null) {
                return Result.error(404, "宠物不存在");
            }
            if (existing.getUserId() == null) {
                return Result.error(400, "宠物数据异常");
            }
            if (!ownerId.equals(existing.getUserId().longValue())) {
                return Result.error(403, "无权修改该宠物");
            }
            pet.setUserId(existing.getUserId());

            logger.info("宠物ID: {}, userId: {}", pet.getId(), pet.getUserId());

            Pet updatedPet = petService.updatePet(pet);
            logger.info("宠物更新成功，ID: {}, 已自动清除缓存", updatedPet.getId());
            logger.info("====================================");
            
            return Result.success(updatedPet);
        } catch (Exception e) {
            logger.error("更新宠物失败: {}", e.getMessage(), e);
            return Result.error("更新宠物失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物")
    public Result<Boolean> deletePet(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            logger.info("========== 删除宠物 ==========");
            logger.info("宠物ID: {}", id);

            Long ownerId = resolveOwnerId(request, authHeader);
            if (ownerId == null) {
                return Result.error(401, "请先登录");
            }
            Pet existing = petService.getPetById(id);
            if (existing == null) {
                return Result.error(404, "宠物不存在");
            }
            if (existing.getUserId() == null) {
                return Result.error(400, "宠物数据异常");
            }
            if (!ownerId.equals(existing.getUserId().longValue())) {
                return Result.error(403, "无权删除该宠物");
            }

            boolean result = petService.deletePet(id);
            if (result) {
                logger.info("宠物删除成功，ID: {}, 已自动清除缓存", id);
            } else {
                logger.warn("宠物删除失败，ID: {}", id);
            }
            logger.info("====================================");
            
            return Result.success(result);
        } catch (Exception e) {
            logger.error("删除宠物失败: {}", e.getMessage(), e);
            return Result.error("删除宠物失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物详情")
    public Result<Pet> getPetDetail(@PathVariable Long id) {
        try {
            logger.info("查询宠物详情，ID: {}", id);
            Pet pet = petService.getPetById(id);
            if (pet == null) {
                logger.warn("未找到宠物，ID: {}", id);
                return Result.error("宠物不存在");
            }

            return Result.success(pet);
        } catch (Exception e) {
            logger.error("查询宠物详情失败: {}", e.getMessage(), e);
            return Result.error("查询宠物详情失败: " + e.getMessage());
        }
    }
}
