package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.StoreWarehouse;
import com.pethome.mapper.StoreWarehouseMapper;
import com.pethome.service.order.StoreWarehouseService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StoreWarehouseServiceImpl extends ServiceImpl<StoreWarehouseMapper, StoreWarehouse>
        implements StoreWarehouseService {

    @Override
    public List<StoreWarehouse> listAll() {
        QueryWrapper<StoreWarehouse> q = new QueryWrapper<>();
        q.orderByAsc("id");
        List<StoreWarehouse> list = list(q);
        return list != null ? list : new ArrayList<>();
    }

    @Override
    public List<StoreWarehouse> listByStore(Long storeId) {
        if (storeId == null) {
            return new ArrayList<>();
        }
        QueryWrapper<StoreWarehouse> q = new QueryWrapper<>();
        q.eq("store_id", storeId).orderByAsc("id");
        List<StoreWarehouse> list = list(q);
        return list != null ? list : new ArrayList<>();
    }
}
