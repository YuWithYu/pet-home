package com.pethome.service.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.StoreWarehouse;

import java.util.List;

/**
 * 店铺发货仓服务。
 */
public interface StoreWarehouseService extends IService<StoreWarehouse> {

    /** 全部发货仓（平台管理员视角），按 id 升序 */
    List<StoreWarehouse> listAll();

    /** 指定店铺的发货仓，按 id 升序 */
    List<StoreWarehouse> listByStore(Long storeId);
}
