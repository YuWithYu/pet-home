package com.pethome.controller.order;


import com.pethome.common.BusinessException;
import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.entity.PointsRecord;
import com.pethome.security.UserTokenResolver;
import com.pethome.service.order.PointsExchangeService;
import com.pethome.service.order.PointsRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@Tag(name = "积分商城")
public class PointsMallController {

    private static final Logger log = LoggerFactory.getLogger(PointsMallController.class);

    @Autowired
    private PointsExchangeService pointsExchangeService;

    @Autowired
    private PointsRecordService pointsRecordService;

    @Autowired
    private UserTokenResolver userTokenResolver;

    /**
     * 获取积分商城商品列表
     */
    @GetMapping("/products")
    @Operation(summary = "获取积分商城商品列表")
    public Result<Map<String, Object>> getPointsMallProducts(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            return Result.success(pointsExchangeService.listMallProducts());
        }, "获取商品列表失败，请稍后重试", log);
    }

    /**
     * 兑换积分商品
     * <p>扣积分/扣库存/建订单的事务边界在 {@link PointsExchangeService}，异常向上传播即回滚。
     */
    @PostMapping("/exchange")
    @Operation(summary = "兑换积分商品")
    public Result<Map<String, Object>> exchangeProduct(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> requestData) {
        Long userId = userTokenResolver.getUserIdFromBearer(token);
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return SafeCall.of(() -> {
            try {
                return Result.success(pointsExchangeService.exchange(userId, requestData));
            } catch (IllegalArgumentException | IllegalStateException e) {
                // 业务校验失败 / 流程中断：事务已在服务层回滚，转为统一业务异常出口
                throw new BusinessException(e.getMessage());
            }
        }, "兑换失败，请稍后重试", log);
    }

    /**
     * 获取兑换记录
     */
    @GetMapping("/exchange/history")
    @Operation(summary = "获取兑换记录")
    public Result<Map<String, Object>> getExchangeHistory(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：与订单列表（#50）策略对齐——userId 只认登录态，
            // 不再 fallback 到 query 参数（否则可越权查询他人兑换记录）。
            // 该接口已从 RoleInterceptor 匿名白名单移除，未登录请求会被 401 拦截。
            Long userId = userTokenResolver.getUserIdFromBearer(token);
            if (userId == null) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("records", new ArrayList<>());
                empty.put("total", 0);
                empty.put("page", page);
                empty.put("size", size);
                return Result.success(empty);
            }

            // 查询兑换记录（通过 description 判断，因为 source 字段不在数据库中）
            List<PointsRecord> records = pointsRecordService.getSpendRecords(userId);

            List<PointsRecord> exchangeRecords = new ArrayList<>();
            for (PointsRecord record : records) {
                String description = record.getDescription();
                // 兼容历史数据「兑换商品:」与当前写入「兑换：」两种描述前缀
                if (description != null
                        && (description.startsWith("兑换商品:") || description.startsWith("兑换："))) {
                    exchangeRecords.add(record);
                }
            }

            // 分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, exchangeRecords.size());
            List<PointsRecord> pagedRecords = exchangeRecords.subList(Math.min(start, exchangeRecords.size()), end);

            List<Map<String, Object>> recordList = new ArrayList<>();
            for (PointsRecord record : pagedRecords) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("description", record.getDescription());
                recordMap.put("points", record.getPoints());
                recordMap.put("createTime", record.getCreateTime());
                recordList.add(recordMap);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("records", recordList);
            result.put("total", exchangeRecords.size());
            result.put("page", page);
            result.put("size", size);

            return Result.success(result);
        }, "获取兑换记录失败，请稍后重试", log);
    }
}
