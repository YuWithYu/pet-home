package com.pethome.service.order.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模拟物流数据生成：地址解析、送达时效估算、全链路轨迹与快递员/取件信息生成。
 */
@Service
public class LogisticsMockDataGenerator {

    @Autowired
    private LogisticsTrackParser trackParser;

    private static final List<String> PROVINCE_NAMES = Arrays.asList(
            "广东", "新疆", "西藏", "黑龙江", "内蒙古", "北京", "上海", "天津", "重庆", "海南", "广西",
            "福建", "江西", "湖南", "浙江", "江苏", "山东", "河北", "河南", "湖北", "四川", "云南",
            "贵州", "陕西", "甘肃", "青海", "宁夏", "辽宁", "吉林", "山西", "安徽");

    /**
     * 生成模拟物流数据（用于演示和测试）
     * 根据收货地址和发货仓地址动态生成物流轨迹
     * 根据发货时间一天一天更新，只显示到当前时间为止的轨迹
     */
    public Map<String, Object> generate(String shippingCompany, String shippingNumber, String receiverAddress, String warehouseAddress, LocalDateTime shippingTime) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tracks = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String numberSeed = shippingNumber != null ? shippingNumber : String.valueOf(System.currentTimeMillis());
        // 解析收货地址（使用正确的解析方法）
        String[] receiverParts = parseAddress(receiverAddress);
        String receiverProvince = receiverParts[0];
        String receiverCity = receiverParts[1];
        String receiverDistrict = receiverParts[2];
        if (receiverAddress == null || receiverAddress.trim().isEmpty()) {
            String[] mockReceiver = pickMockAddress(numberSeed, 0);
            receiverProvince = mockReceiver[0];
            receiverCity = mockReceiver[1];
            receiverDistrict = mockReceiver[2];
        }
        // 解析发货仓地址（如果提供）
        String warehouseProvince;
        String warehouseCity;
        String warehouseDistrict;
        if (warehouseAddress != null && !warehouseAddress.trim().isEmpty()) {
            String[] warehouseParts = parseAddress(warehouseAddress);
            warehouseProvince = warehouseParts[0];
            warehouseCity = warehouseParts[1];
            warehouseDistrict = warehouseParts[2];
        } else {
            // 发货仓按单号动态生成，且强制与收货城市不同
            int offset = 1;
            String[] warehouseMock = pickMockAddress(numberSeed, offset);
            while (warehouseMock[1].equals(receiverCity) && offset < 12) {
                offset++;
                warehouseMock = pickMockAddress(numberSeed, offset);
            }
            warehouseProvince = warehouseMock[0];
            warehouseCity = warehouseMock[1];
            warehouseDistrict = warehouseMock[2];
        }
        String companyName = shippingCompany != null ? shippingCompany : "物流公司";
        String courierName = generateCourierName(shippingNumber);
        String courierPhone = generateCourierPhone(shippingNumber);
        String stationPhone = generateStationPhone(shippingNumber);
        String pickupCode = generatePickupCode(shippingNumber);
        // 按距离规则：同省1-2天、邻省2-3天、隔两省及以上3-4天、极远(如广东↔新疆)5-6天
        int totalDays = computeDeliveryDays(warehouseProvince, receiverProvince);
        LocalDateTime now = LocalDateTime.now();
        // 使用订单的发货时间；若为空或晚于/接近当前时间，则用「当前时间往前推」保证能生成已发生的轨迹
        LocalDateTime shipTime;
        if (shippingTime != null && shippingTime.isBefore(now.minusHours(2))) {
            shipTime = shippingTime;
        } else {
            shipTime = now.minusDays(totalDays).minusHours(2);
        }
        // 全链路模板：下单 -> 拣货 -> 打包 -> 发货 -> 运输 -> 派送 -> 待取件 -> 已签收
        int totalHours = Math.max(24, totalDays * 24);
        int[] hourOffsets = new int[]{
            -18, -16, -14, -12, -10, -8, -6, 0,
            Math.max(2, totalHours / 5),
            Math.max(4, totalHours / 3),
            Math.max(6, totalHours / 2),
            Math.max(8, (int)(totalHours * 0.75)),
            Math.max(10, totalHours - 8),
            totalHours - 3,
            totalHours
        };
        String originNode = warehouseCity + (warehouseDistrict != null ? warehouseDistrict : "") + "集散点";
        String destNode = receiverCity + (receiverDistrict != null ? receiverDistrict : "") + "快递员点";
        String companyTag = "【" + companyName + "】";
        List<String> templates = Arrays.asList(
            "已下单 订单确认，已通知商家配货",
            "拣货中 商家已打印拣货单",
            "拣货中 您的订单开始拣货",
            "已打包 您的订单已验货完成",
            "已打包 您的订单已打包完成",
            "已发货 商家已发货，正在通知" + companyName + "包裹取件",
            "运输中 中国邮政已收取快件",
            "运输中 快件离开【" + warehouseCity + "揽收部】，正在发往【" + originNode + "】",
            "运输中 快件到达【" + originNode + "】",
            "运输中 快件已在【" + originNode + "】完成分拣，准备发出",
            "运输中 快件离开【" + originNode + "】，正在发往【" + destNode + "】",
            "派送中 快件正在派送中，请耐心等待，保持电话畅通，如有疑问请电联快递员【" + courierName + "，电话:" + courierPhone + "】或揽投部【电话:" + stationPhone + "】",
            "待取件 您的快件已派送至【" + destNode + "】，自提点电话:" + stationPhone + "，请凭取件码" + pickupCode + "领取快件",
            "已签收 您的快件已取走【" + destNode + "】，如有疑问请电联快递员【电话:" + courierPhone + "】",
            "已签收 中国邮政将全心呵护您的所托，服务热线11183。"
        );
        for (int i = 0; i < templates.size(); i++) {
            LocalDateTime t = shipTime.plusHours(hourOffsets[i]);
            if (t.isBefore(now) || t.isEqual(now)) {
                Map<String, Object> track = new HashMap<>();
                track.put("trackingTime", t.format(formatter));
                String info = templates.get(i);
                if (i >= 8) {
                    info = companyTag + " " + info;
                }
                track.put("trackingInfo", info);
                if (i <= 8) {
                    track.put("location", warehouseProvince + warehouseCity + warehouseDistrict);
                } else if (i <= 12) {
                    track.put("location", receiverProvince + receiverCity);
                } else {
                    track.put("location", receiverProvince + receiverCity + receiverDistrict);
                }
                tracks.add(track);
            }
        }
        // 若因时间条件未生成任何轨迹，至少补一条「已从xx发出」避免页面显示“暂无物流轨迹”
        if (tracks.isEmpty()) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("trackingTime", now.minusHours(1).format(formatter));
            fallback.put("trackingInfo", "【" + companyName + "】快件已从【" + warehouseCity + "】发出，正在发往【" + receiverCity + "】");
            fallback.put("location", warehouseProvince + warehouseCity);
            tracks.add(fallback);
        }
        // 反转列表，让最新的（签收）在最前面
        Collections.reverse(tracks);
        // 根据是否有签收记录设置状态
        String state = trackParser.resolveMockState(tracks);
        String isCheck = state.equals("3") ? "1" : "0";
        result.put("tracks", tracks);
        result.put("state", state);
        result.put("isCheck", isCheck);
        result.put("company", shippingCompany);
        result.put("number", shippingNumber);
        result.put("estimatedDays", totalDays);
        // 若已过预计送达日，不再显示「预计X天送达」，改为提示应已送达
        LocalDate expectedDate = shipTime.plusDays(totalDays).toLocalDate();
        String tip;
        if (!now.toLocalDate().isBefore(expectedDate)) {
            tip = "快递已送达，请留意物流或联系快递";
        } else {
            tip = "预计" + totalDays + "天送达";
        }
        result.put("estimatedDeliveryTip", tip);
        return result;
    }

    /**
     * 正确解析地址，避免"广东省上海市"这种错误；直辖市：北京、上海、天津、重庆
     */
    private String[] parseAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return new String[]{"广东省", "广州市", "荔湾区"};
        }
        String province = "";
        String city = "";
        String district = "";
        boolean isDirectCity = false;
        for (String dc : new String[]{"北京市", "上海市", "天津市", "重庆市"}) {
            if (address.startsWith(dc)) {
                province = dc; // 直辖市省份和城市相同
                city = dc;
                isDirectCity = true;
                district = extractDistrict(address.substring(dc.length()));
                break;
            }
        }
        if (!isDirectCity) {
            if (address.contains("省")) {
                int provinceEnd = address.indexOf("省") + 1;
                province = address.substring(0, provinceEnd);
                String remaining = address.substring(provinceEnd);
                if (remaining.contains("市")) {
                    int cityEnd = remaining.indexOf("市") + 1;
                    city = remaining.substring(0, cityEnd);
                    district = extractDistrict(remaining.substring(cityEnd));
                }
            } else if (address.contains("市")) {
                // 没有省份，只有城市（可能是省略了省份）
                int cityEnd = address.indexOf("市") + 1;
                city = address.substring(0, cityEnd);
                // 尝试推断省份（简化处理，使用默认）
                province = "广东省"; // 默认值
            }
        }
        if (province.isEmpty()) province = "广东省";
        if (city.isEmpty()) city = "广州市";
        if (district.isEmpty()) district = "荔湾区";
        return new String[]{province, city, district};
    }
    /** 从剩余地址串中截取到最近的「区」或「县」为止 */
    private static String extractDistrict(String remaining) {
        if (remaining == null) return "";
        int idx = remaining.indexOf("区");
        if (idx < 0) {
            idx = remaining.indexOf("县");
        }
        return idx >= 0 ? remaining.substring(0, idx + 1) : "";
    }

    /**
     * 根据发货仓省份与收货省份计算预计送达天数（模拟规则）：
     * 同省 1-2 天，贴着发货仓的邻省 2-3 天，隔两省及以上 3-4 天，极远（如广东↔新疆）5-6 天。
     */
    private int computeDeliveryDays(String warehouseProvince, String receiverProvince) {
        String w = normalizeProvince(warehouseProvince);
        String r = normalizeProvince(receiverProvince);
        if (w == null || r == null) return 3 + (int)(Math.random() * 2);
        if (w.equals(r)) return 1 + (int)(Math.random() * 2); // 同省 1-2 天
        if (isVeryFar(w, r)) return 5 + (int)(Math.random() * 2); // 极远 5-6 天
        if (isAdjacentProvince(w, r)) return 2 + (int)(Math.random() * 2); // 邻省 2-3 天
        return 3 + (int)(Math.random() * 2); // 隔两省及以上 3-4 天
    }

    private static String normalizeProvince(String province) {
        if (province == null || province.isEmpty()) return null;
        for (String p : PROVINCE_NAMES) {
            if (province.contains(p)) return p;
        }
        // 取前两字作为兜底（如 广东省→广东）
        if (province.length() >= 2) return province.substring(0, 2);
        return province;
    }

    private static final Set<String> REMOTE_PROVINCES = new HashSet<>(Arrays.asList("新疆", "西藏", "黑龙江", "内蒙古", "青海", "甘肃", "宁夏"));
    private static final Set<String> EAST_SOUTH_PROVINCES = new HashSet<>(Arrays.asList("广东", "福建", "浙江", "上海", "北京", "江苏", "海南", "江西", "湖南", "湖北", "安徽"));

    /** 极远：东西/南北跨度大，如广东↔新疆、北京↔新疆 */
    private static boolean isVeryFar(String w, String r) {
        return (REMOTE_PROVINCES.contains(w) && EAST_SOUTH_PROVINCES.contains(r))
            || (REMOTE_PROVINCES.contains(r) && EAST_SOUTH_PROVINCES.contains(w));
    }

    /** 邻省关系（部分常见，用于 2-3 天） */
    private static final Map<String, Set<String>> ADJACENT_MAP = new HashMap<>();
    static {
        ADJACENT_MAP.put("广东", new HashSet<>(Arrays.asList("福建", "江西", "湖南", "广西", "海南")));
        ADJACENT_MAP.put("北京", new HashSet<>(Arrays.asList("河北", "天津")));
        ADJACENT_MAP.put("上海", new HashSet<>(Arrays.asList("江苏", "浙江")));
        ADJACENT_MAP.put("新疆", new HashSet<>(Arrays.asList("甘肃", "青海", "西藏")));
        ADJACENT_MAP.put("四川", new HashSet<>(Arrays.asList("云南", "贵州", "陕西", "甘肃", "青海", "西藏", "重庆", "湖北", "湖南")));
        ADJACENT_MAP.put("陕西", new HashSet<>(Arrays.asList("山西", "河南", "湖北", "四川", "甘肃", "宁夏", "内蒙古")));
        ADJACENT_MAP.put("河南", new HashSet<>(Arrays.asList("河北", "山西", "陕西", "湖北", "安徽", "山东", "江苏")));
        ADJACENT_MAP.put("湖北", new HashSet<>(Arrays.asList("河南", "陕西", "四川", "湖南", "江西", "安徽", "重庆")));
        ADJACENT_MAP.put("湖南", new HashSet<>(Arrays.asList("湖北", "江西", "广东", "广西", "贵州", "重庆")));
        ADJACENT_MAP.put("浙江", new HashSet<>(Arrays.asList("上海", "江苏", "安徽", "江西", "福建")));
        ADJACENT_MAP.put("江苏", new HashSet<>(Arrays.asList("上海", "浙江", "安徽", "山东", "河南")));
        ADJACENT_MAP.put("山东", new HashSet<>(Arrays.asList("河北", "河南", "江苏", "安徽")));
        ADJACENT_MAP.put("福建", new HashSet<>(Arrays.asList("浙江", "江西", "广东")));
        ADJACENT_MAP.put("江西", new HashSet<>(Arrays.asList("福建", "浙江", "安徽", "湖北", "湖南", "广东")));
        ADJACENT_MAP.put("广西", new HashSet<>(Arrays.asList("广东", "湖南", "贵州", "云南")));
        ADJACENT_MAP.put("云南", new HashSet<>(Arrays.asList("四川", "贵州", "广西", "西藏")));
        ADJACENT_MAP.put("贵州", new HashSet<>(Arrays.asList("四川", "湖南", "广西", "云南", "重庆")));
        ADJACENT_MAP.put("辽宁", new HashSet<>(Arrays.asList("河北", "内蒙古", "吉林")));
        ADJACENT_MAP.put("吉林", new HashSet<>(Arrays.asList("辽宁", "内蒙古", "黑龙江")));
        ADJACENT_MAP.put("黑龙江", new HashSet<>(Arrays.asList("吉林", "内蒙古")));
        ADJACENT_MAP.put("内蒙古", new HashSet<>(Arrays.asList("黑龙江", "吉林", "辽宁", "河北", "山西", "陕西", "宁夏", "甘肃")));
        ADJACENT_MAP.put("甘肃", new HashSet<>(Arrays.asList("内蒙古", "宁夏", "陕西", "四川", "青海", "新疆")));
        ADJACENT_MAP.put("青海", new HashSet<>(Arrays.asList("甘肃", "新疆", "西藏", "四川")));
        ADJACENT_MAP.put("宁夏", new HashSet<>(Arrays.asList("内蒙古", "陕西", "甘肃")));
        ADJACENT_MAP.put("西藏", new HashSet<>(Arrays.asList("新疆", "青海", "四川", "云南")));
        ADJACENT_MAP.put("海南", new HashSet<>(Arrays.asList("广东")));
        ADJACENT_MAP.put("重庆", new HashSet<>(Arrays.asList("四川", "贵州", "湖南", "湖北", "陕西")));
        ADJACENT_MAP.put("河北", new HashSet<>(Arrays.asList("北京", "天津", "辽宁", "内蒙古", "山西", "河南", "山东")));
        ADJACENT_MAP.put("山西", new HashSet<>(Arrays.asList("河北", "内蒙古", "陕西", "河南")));
        ADJACENT_MAP.put("安徽", new HashSet<>(Arrays.asList("江苏", "浙江", "江西", "湖北", "河南", "山东")));
        ADJACENT_MAP.put("天津", new HashSet<>(Arrays.asList("北京", "河北")));
    }

    private static boolean isAdjacentProvince(String w, String r) {
        Set<String> adj = ADJACENT_MAP.get(w);
        if (adj != null && adj.contains(r)) return true;
        adj = ADJACENT_MAP.get(r);
        return adj != null && adj.contains(w);
    }

    private String generateCourierName(String shippingNumber) {
        String[] names = {"李家亮", "王志强", "陈海涛", "张师傅"};
        int idx = Math.abs((shippingNumber != null ? shippingNumber : "0").hashCode()) % names.length;
        return names[idx];
    }

    private String[] pickMockAddress(String seed, int offset) {
        String[][] candidates = new String[][]{
            {"广东省", "深圳市", "宝安区"},
            {"广东省", "广州市", "荔湾区"},
            {"浙江省", "杭州市", "余杭区"},
            {"江苏省", "苏州市", "工业园区"},
            {"湖北省", "武汉市", "洪山区"},
            {"四川省", "成都市", "武侯区"},
            {"陕西省", "西安市", "雁塔区"},
            {"福建省", "厦门市", "湖里区"},
            {"山东省", "青岛市", "市南区"},
            {"河南省", "郑州市", "金水区"},
            {"湖南省", "长沙市", "岳麓区"},
            {"广西壮族自治区", "南宁市", "青秀区"},
            {"北京市", "北京市", "朝阳区"},
            {"上海市", "上海市", "浦东新区"}
        };
        int idx = Math.abs((seed + "#" + offset).hashCode()) % candidates.length;
        return candidates[idx];
    }

    private String generateCourierPhone(String shippingNumber) {
        String digits = (shippingNumber == null ? "" : shippingNumber.replaceAll("\\D", ""));
        String tail = (digits + "9178512239");
        tail = tail.substring(Math.max(0, tail.length() - 9));
        return "1" + tail;
    }

    private String generateStationPhone(String shippingNumber) {
        String digits = (shippingNumber == null ? "" : shippingNumber.replaceAll("\\D", ""));
        String tail = (digits + "9899191575");
        tail = tail.substring(Math.max(0, tail.length() - 9));
        return "1" + tail;
    }

    private String generatePickupCode(String shippingNumber) {
        String digits = (shippingNumber == null ? "" : shippingNumber.replaceAll("\\D", ""));
        if (digits.length() < 4) digits = (digits + "1688");
        String tail = digits.substring(Math.max(0, digits.length() - 4));
        return tail.substring(0, 2) + "-" + tail.substring(2);
    }
}
