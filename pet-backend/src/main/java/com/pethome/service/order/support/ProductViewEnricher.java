package com.pethome.service.order.support;

import com.pethome.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品展示字段补齐：店铺名称、销量、站点根地址与默认店铺图片。
 */
@Component
public class ProductViewEnricher {

    private static final Logger logger = LoggerFactory.getLogger(ProductViewEnricher.class);

    @Autowired
    private com.pethome.mapper.StoreMapper storeMapper;

    @Autowired(required = false)
    private com.pethome.mapper.OrderItemMapper orderItemMapper;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Value("${upload.http-url:http://localhost:8080/upload/}")
    private String uploadHttpUrl;

    /** 与 BannerController 等一致：优先 app.public-base-url，否则从 upload.http-url 推导站点根 */
    public String publicSiteOrigin() {
        if (publicBaseUrl != null && !publicBaseUrl.trim().isEmpty()) {
            return publicBaseUrl.trim().replaceAll("/+$", "");
        }
        String u = uploadHttpUrl != null ? uploadHttpUrl.trim() : "http://localhost:8080/upload/";
        if (!u.endsWith("/")) {
            u += "/";
        }
        if (u.toLowerCase().endsWith("upload/")) {
            u = u.substring(0, u.length() - "upload/".length());
        }
        return u.replaceAll("/+$", "");
    }

    public String defaultStoreImageUrl() {
        return publicSiteOrigin() + "/upload/store/store-20260117001700-cb17d3ce.png";
    }

    /**
     * 批量填充店铺名称与销量：单次 selectBatchIds + 单次 GROUP BY 替代逐商品查询，消除列表页 N+1
     */
    public void fillStoreAndSale(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        // 批量查询店铺名称
        Map<Long, com.pethome.entity.Store> storeMap = Collections.emptyMap();
        if (storeMapper != null) {
            Set<Long> storeIds = new HashSet<>();
            for (Product product : products) {
                if (product.getStoreId() != null) {
                    storeIds.add(product.getStoreId());
                }
            }
            if (!storeIds.isEmpty()) {
                try {
                    storeMap = storeMapper.selectBatchIds(storeIds).stream()
                            .collect(Collectors.toMap(com.pethome.entity.Store::getId, store -> store));
                } catch (Exception e) {
                    logger.warn("批量加载店铺信息失败: {}", e.getMessage());
                }
            }
        }

        // 批量统计销量（直接统计所有订单项数量，简化处理）
        Map<Long, Integer> saleMap = countSalesByProductIds(products);

        for (Product product : products) {
            // 设置店铺名称：店铺不存在或无店铺ID 时清空
            if (product.getStoreId() != null) {
                com.pethome.entity.Store store = storeMap.get(product.getStoreId());
                product.setStoreName(store != null ? store.getName() : null);
            } else {
                product.setStoreName(null);
            }

            // 设置销量
            Integer sale = saleMap.get(product.getId());
            product.setSale(sale != null ? sale : 0);
        }
    }

    /**
     * 批量统计商品销量：单次 GROUP BY 替代逐商品 selectCount
     */
    public Map<Long, Integer> countSalesByProductIds(List<Product> products) {
        if (orderItemMapper == null) {
            return Collections.emptyMap();
        }
        List<Long> productIds = new ArrayList<>();
        for (Product product : products) {
            if (product.getId() != null) {
                productIds.add(product.getId());
            }
        }
        if (productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<Long, Integer> saleMap = new HashMap<>();
            for (Map<String, Object> row : orderItemMapper.countByProductIds(productIds)) {
                saleMap.put(((Number) row.get("productId")).longValue(), ((Number) row.get("cnt")).intValue());
            }
            return saleMap;
        } catch (Exception e) {
            logger.warn("批量统计商品销量失败: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
