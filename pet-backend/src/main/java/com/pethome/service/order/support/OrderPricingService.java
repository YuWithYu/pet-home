package com.pethome.service.order.support;

import com.pethome.common.BusinessException;
import com.pethome.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 订单计价支撑：以 DB 中 product.specs 为权威来源解析规格单价，杜绝前端篡改金额。
 */
@Service
public class OrderPricingService {

    private static final Logger log = LoggerFactory.getLogger(OrderPricingService.class);

    /**
     * 安全解析订单项单价：规格价一律以 DB 中 product.specs 为权威来源，
     * 杜绝「前端改 specPrice=0.01 低价下单」的金额篡改。
     * 定价规则与 ProductController.buildSkuStockList 保持一致：
     * - 商品无规格（specs 空）但请求带 specName：规格不存在，拒绝下单；
     * - specName 拆分后的每个规格值必须能在 specs 的某一组 values 中匹配；
     * - 匹配到的规格值若配置了 price，取后匹配的（与笛卡尔积 SKU 生成时的覆盖顺序一致）；
     * - 所有规格值均未配置 price 时，回落商品价 product.price。
     *
     * @param product       DB 中的商品（含 specs JSON）
     * @param specName      前端传入的规格组合名（如 "红色,XL"）
     * @param frontendPrice 前端传入的规格价（仅用于篡改告警日志，不参与定价）
     */
    public BigDecimal resolveUnitPrice(Product product, String specName, BigDecimal frontendPrice) {
        BigDecimal defaultPrice = product.getPrice();
        String specsStr = product.getSpecs();

        boolean hasSpecName = specName != null && !specName.trim().isEmpty();
        if (specsStr == null || specsStr.trim().isEmpty()) {
            if (hasSpecName) {
                // 商品根本没有规格配置，却按规格下单：非法请求
                throw new BusinessException("商品规格不存在: " + product.getName());
            }
            return defaultPrice;
        }

        com.fasterxml.jackson.databind.JsonNode groups;
        try {
            groups = com.pethome.util.JSONUtil.parseTree(specsStr);
        } catch (Exception e) {
            log.error("解析商品specs失败，拒绝下单: productId={}", product.getId(), e);
            throw new BusinessException("商品规格数据异常，请稍后重试");
        }

        // 格式一：specs 为 {"skus":[{spData,price,promotionPrice,...}]}——按 spData 值集合匹配 SKU
        if (groups != null && groups.isObject()) {
            com.fasterxml.jackson.databind.JsonNode skusNode = groups.get("skus");
            if (skusNode != null && skusNode.isArray() && skusNode.size() > 0) {
                if (!hasSpecName) {
                    return defaultPrice;
                }
                return resolveSkuObjectPrice(product, skusNode, specName);
            }
            // 非法对象结构（无 skus）：视作无规格
            if (hasSpecName) {
                throw new BusinessException("商品规格不存在: " + product.getName());
            }
            return defaultPrice;
        }

        if (groups == null || !groups.isArray() || groups.size() == 0) {
            if (hasSpecName) {
                throw new BusinessException("商品规格不存在: " + product.getName());
            }
            return defaultPrice;
        }

        if (!hasSpecName) {
            // 有规格商品但请求未选规格：无规格价可用，回落商品价（与旧版行为一致）
            return defaultPrice;
        }

        // 收集所有规格值 -> price（同名值以先出现为准；一组内同名值本就属同一选项）
        java.util.Map<String, BigDecimal> priceByValue = new java.util.HashMap<>();
        for (int g = 0; g < groups.size(); g++) {
            com.fasterxml.jackson.databind.JsonNode group = groups.get(g);
            if (group == null || !group.isObject()) {
                continue;
            }
            com.fasterxml.jackson.databind.JsonNode values = group.get("values");
            if (values == null || !values.isArray()) {
                continue;
            }
            for (int v = 0; v < values.size(); v++) {
                com.fasterxml.jackson.databind.JsonNode vo = values.get(v);
                String name;
                BigDecimal price = null;
                if (vo != null && vo.isObject()) {
                    name = vo.hasNonNull("name") ? vo.get("name").asText() : null;
                    com.fasterxml.jackson.databind.JsonNode p = vo.get("price");
                    if (p != null && !p.isNull()) {
                        try {
                            price = p.isNumber() ? p.decimalValue() : new BigDecimal(p.asText());
                        } catch (NumberFormatException ignore) {
                        }
                    }
                } else {
                    name = vo != null ? vo.asText() : null;
                }
                if (name != null && !name.trim().isEmpty() && price != null) {
                    priceByValue.putIfAbsent(name.trim(), price);
                }
            }
        }

        // 校验 specName 并计算权威价：起始为商品价，遇到「配置了价的规格值」则覆盖
        // （与 buildSkuStockList 一致：无价规格值不改变价格，只有带价值覆盖）
        String[] parts = specName.split("[,，]");
        BigDecimal resolved = defaultPrice;
        boolean anyMatched = false;
        for (String part : parts) {
            String val = part == null ? "" : part.trim();
            if (val.isEmpty()) {
                continue;
            }
            BigDecimal p = priceByValue.get(val);
            if (p != null) {
                anyMatched = true;
                resolved = p;
            }
        }
        if (!anyMatched && !priceByValue.isEmpty()) {
            // 商品规格配置了价格，但请求的规格一个都没匹配上：非法请求
            // （规格无价的商品 priceByValue 为空，不会误伤正常「无价规格组合按商品价」的流程）
            throw new BusinessException("商品规格不存在: " + product.getName());
        }

        if (frontendPrice != null && frontendPrice.compareTo(resolved) != 0) {
            log.warn("前端规格价与后端不一致，以后端为准: productId={}, specName={}, frontend={}, backend={}",
                    product.getId(), specName, frontendPrice, resolved);
        }
        return resolved;
    }

    /**
     * specs 为 {"skus":[...]} 对象格式时的安全定价：
     * 将 specName 拆分后的规格值集合与各 SKU 的 spData 值集合匹配（顺序无关），
     * 匹配到的 SKU 价（promotionPrice 优先）为权威价；匹配不上则拒绝下单。
     */
    private BigDecimal resolveSkuObjectPrice(Product product, com.fasterxml.jackson.databind.JsonNode skusNode, String specName) {
        java.util.Set<String> wanted = new java.util.LinkedHashSet<>();
        for (String part : specName.split("[,，]")) {
            String v = part == null ? "" : part.trim();
            if (!v.isEmpty()) {
                wanted.add(v);
            }
        }
        for (int i = 0; i < skusNode.size(); i++) {
            com.fasterxml.jackson.databind.JsonNode sku = skusNode.get(i);
            if (sku == null || !sku.isObject()) {
                continue;
            }
            com.fasterxml.jackson.databind.JsonNode spData = sku.get("spData");
            java.util.Set<String> values = new java.util.LinkedHashSet<>();
            if (spData != null && spData.isArray()) {
                for (int j = 0; j < spData.size(); j++) {
                    com.fasterxml.jackson.databind.JsonNode kv = spData.get(j);
                    if (kv != null && kv.isObject() && kv.hasNonNull("value")) {
                        String v = kv.get("value").asText();
                        if (v != null && !v.trim().isEmpty()) {
                            values.add(v.trim());
                        }
                    }
                }
            }
            if (!wanted.isEmpty() && values.equals(wanted)) {
                // promotionPrice 优先（与前端取价口径一致）
                com.fasterxml.jackson.databind.JsonNode promo = sku.get("promotionPrice");
                if (promo != null && !promo.isNull()) {
                    try {
                        return promo.isNumber() ? promo.decimalValue() : new BigDecimal(promo.asText());
                    } catch (NumberFormatException ignore) {
                    }
                }
                com.fasterxml.jackson.databind.JsonNode price = sku.get("price");
                if (price != null && !price.isNull()) {
                    try {
                        return price.isNumber() ? price.decimalValue() : new BigDecimal(price.asText());
                    } catch (NumberFormatException ignore) {
                    }
                }
                return product.getPrice();
            }
        }
        throw new BusinessException("商品规格不存在: " + product.getName());
    }
}
