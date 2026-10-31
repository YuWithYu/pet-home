package com.pethome.service.order.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.StoreWarehouse;
import com.pethome.service.order.OrderWarehouseAppService;
import com.pethome.service.order.StoreWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 发货仓应用服务实现：校验与编排在此，持久化委托 {@link StoreWarehouseService}。
 */
@Service
public class OrderWarehouseAppServiceImpl implements OrderWarehouseAppService {

    @Autowired
    private StoreWarehouseService storeWarehouseService;

    @Override
    public List<StoreWarehouse> listWarehouses(Long currentStoreId, boolean platformAdmin) {
        if (currentStoreId != null) {
            return storeWarehouseService.listByStore(currentStoreId);
        }
        if (platformAdmin) {
            return storeWarehouseService.listAll();
        }
        return new ArrayList<>();
    }

    @Override
    public StoreWarehouse addWarehouse(Map<String, Object> body, Long currentStoreId, boolean platformAdmin) {
        // 平台管理员无 storeId 时，允许从请求体传入当前订单的 storeId
        if (currentStoreId == null && body != null && body.containsKey("storeId")) {
            Object sid = body.get("storeId");
            if (sid instanceof Number) {
                currentStoreId = ((Number) sid).longValue();
            } else if (sid != null && !sid.toString().trim().isEmpty()) {
                try {
                    currentStoreId = Long.parseLong(sid.toString().trim());
                } catch (NumberFormatException ignored) {
                    // 参数格式非法时保持为空，走后续权限校验
                }
            }
        }
        if (currentStoreId == null && !platformAdmin) {
            throw new BusinessException("当前账号无权操作，请使用管理员账号");
        }

        String address = null;
        if (body != null && body.get("address") != null) {
            address = body.get("address").toString().trim();
        }
        if (address == null || address.isEmpty()) {
            throw new BusinessException("请输入发货仓地址");
        }

        StoreWarehouse wh = new StoreWarehouse();
        wh.setStoreId(currentStoreId != null ? currentStoreId : -1L);
        wh.setAddress(address);
        wh.setCreateTime(LocalDateTime.now());
        storeWarehouseService.save(wh);
        return wh;
    }

    @Override
    public void deleteWarehouse(Long id, Long currentStoreId, boolean platformAdmin) {
        StoreWarehouse existing = storeWarehouseService.getById(id);
        if (existing == null) {
            throw new BusinessException("发货仓不存在");
        }
        if (!platformAdmin && currentStoreId != null && !currentStoreId.equals(existing.getStoreId())) {
            throw new BusinessException("无权删除此发货仓");
        }
        storeWarehouseService.removeById(id);
    }
}
