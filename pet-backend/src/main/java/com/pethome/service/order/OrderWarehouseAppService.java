package com.pethome.service.order;

import com.pethome.entity.StoreWarehouse;

import java.util.List;
import java.util.Map;

/**
 * 发货仓应用服务：参数校验与权限编排，内部委托 {@link StoreWarehouseService}。
 */
public interface OrderWarehouseAppService {

    /**
     * 发货仓列表。
     *
     * @param currentStoreId 当前店铺 ID（可为空）
     * @param platformAdmin  当前登录者是否平台管理员
     */
    List<StoreWarehouse> listWarehouses(Long currentStoreId, boolean platformAdmin);

    /**
     * 新增发货仓。
     *
     * @param body           请求体（address、可选 storeId）
     * @param currentStoreId 当前店铺 ID（可为空）
     * @param platformAdmin  当前登录者是否平台管理员
     */
    StoreWarehouse addWarehouse(Map<String, Object> body, Long currentStoreId, boolean platformAdmin);

    /**
     * 删除发货仓。
     */
    void deleteWarehouse(Long id, Long currentStoreId, boolean platformAdmin);
}
