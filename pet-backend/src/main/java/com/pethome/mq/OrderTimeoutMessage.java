package com.pethome.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单超时消息体。
 * 只携带定位业务所需的最小字段，消费端以数据库状态为准（避免消息体被当作可信数据源）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeoutMessage {

    private Long orderId;

    private String orderNo;

    private Long userId;

    /** 下单时间戳，消费端用于二次校验是否真的超时 */
    private Long createTimeMillis;
}
