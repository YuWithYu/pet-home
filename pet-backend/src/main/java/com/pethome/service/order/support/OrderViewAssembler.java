package com.pethome.service.order.support;

import com.pethome.entity.Address;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.Product;
import com.pethome.entity.RefundRequest;
import com.pethome.entity.User;
import com.pethome.service.order.AddressService;
import com.pethome.service.order.RefundRequestService;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单视图装配组件。
 * <p>
 * 负责订单列表/详情返回结构的字段映射、收货信息补齐、退款信息填充与状态码转换，
 * 不参与订单查询本身，便于独立演进与复用。
 */
@Service
public class OrderViewAssembler {

    private static final Logger log = LoggerFactory.getLogger(OrderViewAssembler.class);

    @Autowired(required = false)
    private AddressService addressService;

    @Autowired
    private RefundRequestService refundRequestService;

    @Autowired(required = false)
    private UserService userService;

    /**
     * 订单基础字段（金额、状态、时间、物流、支付方式）组装。
     */
    public Map<String, Object> buildOrderDetailBase(Order order) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("id", order.getId());
        orderData.put("orderNo", order.getOrderNo());
        orderData.put("orderSn", order.getOrderNo()); // 兼容字段
        orderData.put("totalAmount", order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        orderData.put("payAmount", order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        orderData.put("freightAmount", BigDecimal.ZERO); // 运费，暂时为0
        orderData.put("promotionAmount", BigDecimal.ZERO); // 活动优惠，暂时为0
        orderData.put("couponAmount", BigDecimal.ZERO); // 优惠券，暂时为0
        orderData.put("integrationAmount", BigDecimal.ZERO); // 积分抵扣，暂时为0
        orderData.put("note", ""); // 备注

        Integer orderStatus = order.getStatus() != null ? order.getStatus() : 0;
        Integer paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus() : 0;

        orderData.put("status", toOrderStatus(orderStatus));
        orderData.put("statusNumber", orderStatus); // 同时保留数字状态，以备需要
        orderData.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : "");

        // 计算支付时间：如果订单已支付（status>=1 或 paymentStatus==1），使用创建时间
        String paymentTime = null;
        if ((orderStatus >= 1 || paymentStatus == 1) && order.getCreateTime() != null) {
            paymentTime = order.getCreateTime().toString();
        }
        orderData.put("paymentTime", paymentTime);

        // 计算发货时间：优先使用订单中的 shippingTime，否则使用更新时间
        String shippingTime = null;
        if (order.getShippingTime() != null) {
            shippingTime = order.getShippingTime().toString();
        } else if (orderStatus >= 2 && order.getUpdateTime() != null) {
            shippingTime = order.getUpdateTime().toString();
        }
        orderData.put("shippingTime", shippingTime);
        orderData.put("deliveryTime", shippingTime); // 兼容字段

        orderData.put("shippingCompany", order.getShippingCompany() != null ? order.getShippingCompany() : "");
        orderData.put("shippingNumber", order.getShippingNumber() != null ? order.getShippingNumber() : "");
        // 已支付默认显示微信支付
        orderData.put("payType", (orderStatus >= 1 || paymentStatus == 1) ? 2 : 0);

        // 为管理员后台添加兼容的时间字段
        orderData.put("payTime", paymentTime);
        orderData.put("shipTime", shippingTime);
        return orderData;
    }

    /**
     * 单个订单项视图（详情页字段，兼容前端多套字段名）。
     */
    public Map<String, Object> buildOrderItemView(OrderItem item, Product p) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("id", item.getId());
        itemData.put("productId", item.getProductId());
        itemData.put("productName", item.getProductName());
        itemData.put("productPic", item.getProductImage());
        itemData.put("productImage", item.getProductImage());
        itemData.put("productPrice", item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
        itemData.put("price", item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
        itemData.put("productQuantity", item.getQuantity() != null ? item.getQuantity() : 1);
        itemData.put("quantity", item.getQuantity() != null ? item.getQuantity() : 1);
        itemData.put("specName", item.getSpecName() != null ? item.getSpecName() : "");
        itemData.put("productAttr", ""); // 商品属性，暂时为空
        if (p != null && p.getStoreId() != null) {
            itemData.put("storeId", p.getStoreId());
        }
        return itemData;
    }

    /**
     * 订单无明细时的兜底条目（把订单本身当作一条商品记录返回）。
     */
    public Map<String, Object> buildFallbackOrderItem(Order order) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("id", order.getId());
        itemData.put("productId", order.getId());
        itemData.put("productName", "商品订单 #" + order.getOrderNo());
        itemData.put("productPic", "");
        itemData.put("productImage", "");
        itemData.put("productPrice", order.getTotalAmount());
        itemData.put("price", order.getTotalAmount());
        itemData.put("productQuantity", 1);
        itemData.put("quantity", 1);
        itemData.put("specName", "");
        itemData.put("productAttr", "");
        return itemData;
    }

    /**
     * 收货地址填充：优先使用订单快照，缺失时回落到用户默认地址。
     */
    public void fillReceiverAddress(Order order, Map<String, Object> orderData) {
        String receiverName = order.getReceiverName();
        String receiverPhone = order.getReceiverPhone();
        String receiverProvince = order.getReceiverProvince();
        String receiverCity = order.getReceiverCity();
        String receiverRegion = order.getReceiverRegion();
        String receiverDetailAddress = order.getReceiverDetailAddress();

        if (receiverName == null || receiverName.isEmpty()) {
            Address defaultAddress = addressService != null ? addressService.getDefaultAddress(order.getUserId()) : null;
            if (defaultAddress != null) {
                receiverName = defaultAddress.getContactName() != null ? defaultAddress.getContactName() : "";
                receiverPhone = defaultAddress.getContactPhone() != null ? defaultAddress.getContactPhone() : "";
                receiverProvince = defaultAddress.getProvince() != null ? defaultAddress.getProvince() : "";
                receiverCity = defaultAddress.getCity() != null ? defaultAddress.getCity() : "";
                receiverRegion = defaultAddress.getDistrict() != null ? defaultAddress.getDistrict() : "";
                receiverDetailAddress = defaultAddress.getDetail() != null ? defaultAddress.getDetail() : "";
            }
        }

        if (receiverName == null || receiverName.isEmpty()) {
            receiverName = "";
            receiverPhone = "";
            receiverProvince = "";
            receiverCity = "";
            receiverRegion = "";
            receiverDetailAddress = "";
        }

        StringBuilder fullAddress = new StringBuilder();
        if (receiverProvince != null && !receiverProvince.isEmpty()) fullAddress.append(receiverProvince);
        if (receiverCity != null && !receiverCity.isEmpty()) fullAddress.append(receiverCity);
        if (receiverRegion != null && !receiverRegion.isEmpty()) fullAddress.append(receiverRegion);
        if (receiverDetailAddress != null && !receiverDetailAddress.isEmpty()) fullAddress.append(receiverDetailAddress);

        orderData.put("receiverName", receiverName);
        orderData.put("receiverPhone", receiverPhone);
        orderData.put("receiverProvince", receiverProvince);
        orderData.put("receiverCity", receiverCity);
        orderData.put("receiverRegion", receiverRegion);
        orderData.put("receiverDetailAddress", receiverDetailAddress);
        // 为管理员后台添加兼容字段
        orderData.put("phone", receiverPhone);
        orderData.put("address", fullAddress.toString());
    }

    /**
     * 统一补充退款信息（用于 C 端“售后详情”展示申请原因/状态）。
     */
    public void fillRefundInfo(Order order, Map<String, Object> orderData) {
        RefundRequest refundRequest = refundRequestService.getByOrderId(order.getId());
        if (refundRequest != null && !"cancelled".equals(refundRequest.getStatus())) {
            orderData.put("hasRefundRequest", true);
            orderData.put("refundStatus", refundRequest.getStatus());
            orderData.put("refundReason", refundRequest.getReason());
            orderData.put("refundType", refundRequest.getType());
            orderData.put("refundAmount", refundRequest.getRefundAmount() != null ? refundRequest.getRefundAmount() : BigDecimal.ZERO);
            orderData.put("refundRequestId", refundRequest.getId());
            orderData.put("refundCreateTime", refundRequest.getCreateTime() != null ? refundRequest.getCreateTime().toString() : "");
        } else {
            orderData.put("hasRefundRequest", false);
        }
    }

    /**
     * 收货人/电话/地址在订单行未落库时，用默认地址与 C 端用户信息补齐（与 C 端展示一致）。
     */
    public void enrichOrderContactForDisplay(Order order, Map<String, Object> orderData) {
        if (order == null || orderData == null) {
            return;
        }
        Long uid = order.getUserId();
        if (uid == null) {
            return;
        }
        String userPlaceholder = "用户" + uid;
        String customer = (String) orderData.get("customer");
        String phone = orderData.get("phone") != null ? String.valueOf(orderData.get("phone")) : "";
        String addr = orderData.get("address") != null ? String.valueOf(orderData.get("address")) : "";
        boolean needEnrich = (phone != null && phone.isEmpty()) || (addr != null && addr.isEmpty())
                || userPlaceholder.equals(String.valueOf(customer));
        if (addressService != null && needEnrich) {
            Address def = addressService.getDefaultAddress(uid);
            if (def != null) {
                if (userPlaceholder.equals(String.valueOf(customer)) && def.getContactName() != null
                        && !def.getContactName().trim().isEmpty()) {
                    orderData.put("customer", def.getContactName().trim());
                }
                if (phone != null && phone.isEmpty() && def.getContactPhone() != null) {
                    orderData.put("phone", def.getContactPhone().trim());
                }
                if (addr != null && addr.isEmpty()) {
                    StringBuilder full = new StringBuilder();
                    if (def.getProvince() != null) full.append(def.getProvince());
                    if (def.getCity() != null) full.append(def.getCity());
                    if (def.getDistrict() != null) full.append(def.getDistrict());
                    if (def.getDetail() != null) full.append(def.getDetail());
                    orderData.put("address", full.toString());
                }
            }
        }
        customer = (String) orderData.get("customer");
        if (userService != null && (customer == null || customer.isEmpty() || userPlaceholder.equals(customer))) {
            User u = userService.getUserById(uid);
            if (u != null) {
                if (u.getNickname() != null && !u.getNickname().trim().isEmpty()) {
                    orderData.put("customer", u.getNickname().trim());
                } else if (u.getUsername() != null && !u.getUsername().trim().isEmpty()) {
                    orderData.put("customer", u.getUsername().trim());
                }
            }
        }
    }

    /**
     * 状态参数（pending/paid/shipped/completed/cancelled/all）转数据库状态码。
     */
    public Integer toStatusCodeFilter(String status) {
        if (status == null || status.isEmpty() || "all".equals(status)) {
            return null;
        }
        switch (status) {
            case "pending": return 0;
            case "paid": return 1;
            case "shipped": return 2;
            case "completed": return 3;
            case "cancelled": return -1;
            default: return null;
        }
    }

    /**
     * 数据库状态码转前端状态文案。
     */
    public String toOrderStatus(Integer status) {
        if (status == null) {
            return "pending";
        }
        switch (status) {
            case 1: return "paid";
            case 2: return "shipped";
            case 3: return "completed";
            case -1:
            case 4: // 4 为历史遗留的取消值，兼容
                return "cancelled";
            default: return "pending";
        }
    }

    /**
     * 解析请求中的 userId 参数（all/null/非法值均视为未指定）。
     */
    public Long parseUserId(String userIdParam) {
        if (userIdParam == null || userIdParam.isEmpty()
                || "all".equals(userIdParam) || "null".equals(userIdParam)) {
            return null;
        }
        try {
            return Long.parseLong(userIdParam);
        } catch (NumberFormatException e) {
            log.warn("订单列表 - 警告: userId参数格式错误: {}, 将忽略该参数", userIdParam);
            return null;
        }
    }

    /**
     * 空订单列表结构（与正常返回保持相同字段）。
     */
    public Map<String, Object> emptyOrderList() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("orders", new ArrayList<>());
        empty.put("total", 0);
        empty.put("unpaidCount", 0);
        empty.put("paidCount", 0);
        empty.put("shippedCount", 0);
        empty.put("completedCount", 0);
        return empty;
    }
}
