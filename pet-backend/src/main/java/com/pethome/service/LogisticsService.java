package com.pethome.service;

import java.util.List;
import java.util.Map;

public interface LogisticsService {
    /**
     * 查询物流信息
     * @param shippingCompany 物流公司代码
     * @param shippingNumber 物流单号
     * @param receiverAddress 收货地址（用于生成模拟发货地点）
     * @param warehouseAddress 发货仓地址（如果为空则自动生成）
     * @param shippingTime 发货时间（用于计算物流轨迹时间）
     * @return 物流轨迹信息
     */
    Map<String, Object> queryLogistics(String shippingCompany, String shippingNumber, String receiverAddress, String warehouseAddress, java.time.LocalDateTime shippingTime);

    /**
     * 获取支持的物流公司列表
     * @return 物流公司列表
     */
    List<Map<String, String>> getSupportedCompanies();
}
