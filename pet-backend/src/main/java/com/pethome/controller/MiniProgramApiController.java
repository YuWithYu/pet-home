package com.pethome.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.entity.Banner;
import com.pethome.entity.User;
import com.pethome.entity.Category;
import com.pethome.entity.GroomingServiceBanner;
import com.pethome.entity.LitterServiceBanner;
import com.pethome.entity.MedicalServiceBanner;
import com.pethome.entity.Product;
import com.pethome.service.BannerService;
import com.pethome.service.CategoryService;
import com.pethome.service.GroomingServiceBannerService;
import com.pethome.service.LitterServiceBannerService;
import com.pethome.service.MedicalServiceBannerService;
import com.pethome.service.ProductService;
import com.pethome.service.SmsService;
import com.pethome.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 小程序API兼容层控制器
 * 提供与第三方小程序API兼容的接口
 */
@RestController
@RequestMapping("/tz")
@Api(tags = "小程序API兼容层")
public class MiniProgramApiController {

    private static final Logger logger = LoggerFactory.getLogger(MiniProgramApiController.class);

    @Autowired
    private BannerService bannerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroomingServiceBannerService groomingServiceBannerService;

    @Autowired
    private MedicalServiceBannerService medicalServiceBannerService;

    @Autowired
    private LitterServiceBannerService litterServiceBannerService;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.host:http://localhost}")
    private String serverHost;

    /**
     * 将相对路径转换为完整的图片URL
     */
    private String convertToFullUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "";
        }
        
        // 如果已经是完整URL，直接返回
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        
        // 确保路径格式正确
        // 如果路径以 /upload/ 或 /static/ 开头，直接拼接
        if (imagePath.startsWith("/upload/") || imagePath.startsWith("/static/")) {
            return serverHost + ":" + serverPort + imagePath;
        }
        
        // 如果路径以 / 开头但不是 /upload/ 或 /static/，直接拼接（不添加 /upload 前缀）
        if (imagePath.startsWith("/")) {
            return serverHost + ":" + serverPort + imagePath;
        }
        
        // 其他情况（如 product/xxx.jpg），添加 /upload/ 前缀
        return serverHost + ":" + serverPort + "/upload/" + imagePath;
    }

    @GetMapping("/config/values")
    @ApiOperation("获取配置信息")
    public Map<String, Object> getConfigValues(@RequestParam String keys) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        List<Map<String, String>> configList = new java.util.ArrayList<>();

        // 根据keys参数返回相应的配置
        String[] keyArray = keys.split(",");
        for (String key : keyArray) {
            Map<String, String> configItem = new HashMap<>();
            String trimmedKey = key.trim();
            configItem.put("key", trimmedKey);
            
            String value = switch (trimmedKey) {
                case "mallName" -> "宠物之家";
                case "shopMod", "share_profile", "recharge_amount_min", "open_growth" -> "1";
                case "shopping_cart_vop_open", "needIdCheck" -> "0";
                default -> "";
            };
            configItem.put("value", value);
            configList.add(configItem);
        }

        result.put("data", configList);
        return result;
    }

    @GetMapping("/banner/list")
    @ApiOperation("获取轮播图列表")
    public Map<String, Object> getBannerList(@RequestParam(required = false) String type) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        // 从数据库获取轮播图数据
        List<Banner> banners = bannerService.getAllBanners();
        
        // 转换为小程序需要的格式，并处理图片URL
        List<Map<String, Object>> bannerList = banners.stream()
            .filter(banner -> "active".equals(banner.getStatus())) // 只返回激活的轮播图
            .map(banner -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", banner.getId());
                item.put("title", banner.getTitle());
                item.put("description", banner.getDescription());
                
                // 将图片路径转换为完整URL
                String imageUrl = convertToFullUrl(banner.getFileUrl());
                item.put("picUrl", imageUrl);
                item.put("url", imageUrl); // 兼容不同字段名
                item.put("business", "");
                
                return item;
            })
            .collect(Collectors.toList());

        result.put("data", bannerList);
        return result;
    }

    @GetMapping("/grooming-banner")
    @ApiOperation("获取洗护服务展示图")
    public Map<String, Object> getGroomingServiceBanner() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        try {
            // 获取洗护服务页面顶部的展示图
            GroomingServiceBanner banner = groomingServiceBannerService.getBannerByPosition("grooming-page-top");
            
            if (banner != null) {
                Map<String, Object> bannerData = new HashMap<>();
                bannerData.put("id", banner.getId());
                bannerData.put("title", banner.getTitle());
                bannerData.put("description", banner.getDescription());
                bannerData.put("imageUrl", convertToFullUrl(banner.getImageUrl()));
                bannerData.put("position", banner.getPosition());
                bannerData.put("status", banner.getStatus());
                
                result.put("data", bannerData);
            } else {
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取洗护服务展示图失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    @GetMapping("/medical-banner")
    @ApiOperation("获取宠物医院展示图")
    public Map<String, Object> getMedicalServiceBanner() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        try {
            // 获取宠物医院页面顶部的展示图
            MedicalServiceBanner banner = medicalServiceBannerService.getBannerByPosition("medical-page-top");
            
            if (banner != null) {
                Map<String, Object> bannerData = new HashMap<>();
                bannerData.put("id", banner.getId());
                bannerData.put("title", banner.getTitle());
                bannerData.put("description", banner.getDescription());
                bannerData.put("imageUrl", convertToFullUrl(banner.getImageUrl()));
                bannerData.put("position", banner.getPosition());
                bannerData.put("status", banner.getStatus());
                
                result.put("data", bannerData);
            } else {
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取宠物医院展示图失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    @GetMapping("/litter-banner")
    @ApiOperation("获取铲屎服务展示图")
    public Map<String, Object> getLitterServiceBanner() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        try {
            // 获取铲屎服务页面顶部的展示图
            LitterServiceBanner banner = litterServiceBannerService.getBannerByPosition("litter-page-top");
            
            if (banner != null) {
                Map<String, Object> bannerData = new HashMap<>();
                bannerData.put("id", banner.getId());
                bannerData.put("title", banner.getTitle());
                bannerData.put("description", banner.getDescription());
                bannerData.put("imageUrl", convertToFullUrl(banner.getImageUrl()));
                bannerData.put("position", banner.getPosition());
                bannerData.put("status", banner.getStatus());
                
                result.put("data", bannerData);
            } else {
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取铲屎服务展示图失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }

    @PostMapping("/sms/send")
    @ApiOperation("发送短信验证码")
    public Map<String, Object> sendSms(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取手机号，处理可能的数字类型
            String phone = params.get("phone") != null ? params.get("phone").toString() : null;

            if (phone == null || phone.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "手机号不能为空");
                return result;
            }

            // 验证手机号格式
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                result.put("code", 1);
                result.put("msg", "请输入正确的手机号");
                return result;
            }

            // 调用短信服务发送验证码
            String smsResult = smsService.sendVerificationCode(phone);

            if ("success".equals(smsResult)) {
                result.put("code", 0);
                result.put("msg", "验证码发送成功");
            } else {
                result.put("code", 1);
                result.put("msg", "验证码发送失败");
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "发送验证码失败：" + e.getMessage());
            logger.error("发送验证码失败", e);
        }

        return result;
    }

    @PostMapping("/user/wxapp/login")
    @ApiOperation("微信小程序登录")
    public Map<String, Object> wxappLogin(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String code = params.get("code");

            if (code == null || code.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "授权码不能为空");
                return result;
            }

            // 这里应该实现微信小程序登录逻辑
            // 获取微信用户信息，然后生成token

            Map<String, Object> data = new HashMap<>();
            data.put("token", "wx_token_" + System.currentTimeMillis());
            data.put("userInfo", Map.of(
                "uid", 1,
                "nickname", "微信用户",
                "avatar", "/images/default-avatar.png"
            ));

            result.put("code", 0);
            result.put("msg", "登录成功");
            result.put("data", data);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "微信登录失败：" + e.getMessage());
            logger.error("微信登录失败", e);
        }

        return result;
    }

    @PostMapping("/user/wxapp/authorize")
    @ApiOperation("微信小程序授权")
    public Map<String, Object> wxappAuthorize(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        // 这里应该实现微信小程序授权逻辑
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock_token");
        data.put("uid", 1);

        result.put("data", data);
        return result;
    }

    @PostMapping("/user/login")
    @ApiOperation("手机号密码登录")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String phone = loginData.get("phone");
            String password = loginData.get("password");

            if (phone == null || phone.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "手机号不能为空");
                return result;
            }

            if (password == null || password.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "密码不能为空");
                return result;
            }

            // 调用用户服务进行登录验证
            String token = userService.loginByPhone(phone, password);
            if (token != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                
                // 从token中提取用户ID（格式：token_userId_timestamp）
                String[] tokenParts = token.split("_");
                Long userId = 1L;
                if (tokenParts.length >= 2) {
                    try {
                        userId = Long.parseLong(tokenParts[1]);
                        data.put("uid", userId);
                    } catch (NumberFormatException e) {
                        logger.warn("无法从token中解析用户ID，使用默认值", e);
                        data.put("uid", 1L);
                    }
                } else {
                    data.put("uid", 1L);
                }
                
                // 获取用户详细信息
                try {
                    User user = userService.getUserByPhone(phone);
                    if (user != null) {
                        data.put("nickname", user.getNickname());
                        data.put("avatar", user.getAvatar());
                        data.put("phone", user.getPhone());
                    }
                } catch (Exception e) {
                    logger.warn("获取用户信息失败", e);
                }

                result.put("code", 0);
                result.put("msg", "登录成功");
                result.put("data", data);
            } else {
                result.put("code", 1);
                result.put("msg", "手机号或密码错误");
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "登录失败：" + e.getMessage());
            logger.error("登录失败", e);
        }

        return result;
    }

    @PostMapping("/user/update")
    @ApiOperation("更新用户信息")
    public Map<String, Object> updateUser(@RequestBody Map<String, Object> userData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String token = (String) userData.get("token");
            if (token == null || token.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "token不能为空");
                return result;
            }

            // 从token中提取用户ID
            String[] tokenParts = token.split("_");
            Long userId = 1L;
            if (tokenParts.length >= 2) {
                try {
                    userId = Long.parseLong(tokenParts[1]);
                } catch (NumberFormatException e) {
                    result.put("code", 1);
                    result.put("msg", "无效的token");
                    return result;
                }
            }

            // 获取用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                result.put("code", 1);
                result.put("msg", "用户不存在");
                return result;
            }

            // 更新用户信息
            if (userData.containsKey("nickname")) {
                user.setNickname((String) userData.get("nickname"));
            }
            if (userData.containsKey("avatar")) {
                user.setAvatar((String) userData.get("avatar"));
            }
            if (userData.containsKey("gender")) {
                user.setGender((Integer) userData.get("gender"));
            }

            boolean success = userService.updateUser(user);
            if (success) {
                result.put("code", 0);
                result.put("msg", "更新成功");
            } else {
                result.put("code", 1);
                result.put("msg", "更新失败");
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新失败：" + e.getMessage());
            logger.error("更新用户信息失败", e);
        }

        return result;
    }

    @PostMapping("/user/register")
    @ApiOperation("用户注册")
    public Map<String, Object> register(@RequestBody Map<String, Object> registerData) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取参数，处理可能的数字类型
            String phone = registerData.get("phone") != null ? registerData.get("phone").toString() : null;
            String password = registerData.get("password") != null ? registerData.get("password").toString() : null;
            String nickname = registerData.get("nickname") != null ? registerData.get("nickname").toString() : null;
            String smsCode = registerData.get("smsCode") != null ? registerData.get("smsCode").toString() : null;

            if (phone == null || phone.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "手机号不能为空");
                return result;
            }

            if (password == null || password.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "密码不能为空");
                return result;
            }

            if (password.length() < 6) {
                result.put("code", 1);
                result.put("msg", "密码长度不能少于6位");
                return result;
            }

            if (smsCode == null || smsCode.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "验证码不能为空");
                return result;
            }

            // 验证验证码（这里简化处理，实际应该调用短信服务验证）
            if (!smsService.verifyCode(phone, smsCode)) {
                result.put("code", 1);
                result.put("msg", "验证码错误或已过期");
                return result;
            }

            // 调用用户服务进行注册
            String token = userService.register(phone, password, nickname);
            if (token != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                // 从token中提取用户ID
                String[] tokenParts = token.split("_");
                if (tokenParts.length >= 2) {
                    try {
                        data.put("uid", Long.parseLong(tokenParts[1]));
                    } catch (NumberFormatException e) {
                        logger.warn("无法从token中解析用户ID，使用默认值", e);
                        data.put("uid", 1L);
                    }
                } else {
                    data.put("uid", 1L);
                }
                data.put("nickname", nickname != null ? nickname : phone);
                data.put("avatar", "");

                result.put("code", 0);
                result.put("msg", "注册成功");
                result.put("data", data);
            } else {
                result.put("code", 1);
                result.put("msg", "注册失败，手机号可能已存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "注册失败：" + e.getMessage());
            logger.error("注册失败", e);
        }

        return result;
    }

    @GetMapping("/user/detail")
    @ApiOperation("获取用户详情")
    public Map<String, Object> getUserDetail(@RequestParam(required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        // 这里应该根据token获取用户信息，这里返回模拟数据
        Map<String, Object> user = new HashMap<>();
        user.put("uid", 1);
        user.put("nick", "宠物主人");
        user.put("avatar", "/static/images/empty.jpg");
        user.put("mobile", "");
        user.put("gender", 0);

        result.put("data", user);
        return result;
    }

    @GetMapping("/user/amount")
    @ApiOperation("获取用户资产")
    public Map<String, Object> getUserAmount(@RequestParam(required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        Map<String, Object> amount = new HashMap<>();
        amount.put("balance", "0.00");
        amount.put("freeze", "0.00");
        amount.put("score", 0);
        amount.put("growth", 0);

        result.put("data", amount);
        return result;
    }

    @GetMapping("/notice/last-one")
    @ApiOperation("获取最新公告")
    public Map<String, Object> getLastNotice(@RequestParam(required = false) String type) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        Map<String, Object> notice = new HashMap<>();
        notice.put("id", 1);
        notice.put("title", "欢迎来到宠物之家");
        notice.put("content", "感谢您选择宠物之家，我们致力于为您的宠物提供最好的产品和服务。");
        notice.put("createTime", System.currentTimeMillis());

        result.put("data", notice);
        return result;
    }

    @GetMapping("/shop/goods/category/all")
    @ApiOperation("获取所有商品分类")
    public Map<String, Object> getAllCategories() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        List<Category> categories = categoryService.getAllCategories();
        result.put("data", categories);

        return result;
    }

    @GetMapping("/shop/goods/recommend")
    @ApiOperation("获取推荐商品")
    public Map<String, Object> getRecommendProducts(@RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        // 获取推荐商品列表
        List<Product> products = productService.getHotProducts(limit);
        
        // 处理商品图片URL
        List<Map<String, Object>> goodsList = products.stream()
            .map(product -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", product.getId());
                item.put("name", product.getName());
                item.put("description", product.getDescription());
                item.put("price", product.getPrice());
                item.put("originalPrice", product.getPrice()); // 原价
                item.put("stock", product.getStock());
                item.put("category", product.getCategory());
                
                // 将图片路径转换为完整URL
                String imageUrl = convertToFullUrl(product.getImage());
                item.put("pic", imageUrl);
                item.put("image", imageUrl);
                item.put("minPrice", product.getPrice());
                item.put("status", product.getStatus());
                
                return item;
            })
            .collect(Collectors.toList());

        result.put("data", goodsList);
        return result;
    }

    @PostMapping("/shop/goods/list/v2")
    @ApiOperation("获取商品列表V2")
    public Map<String, Object> getGoodsList(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        // 获取商品列表
        List<Product> products = productService.getHotProducts(20);
        
        // 处理商品图片URL
        List<Map<String, Object>> goodsList = products.stream()
            .map(product -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", product.getId());
                item.put("name", product.getName());
                item.put("description", product.getDescription());
                item.put("price", product.getPrice());
                item.put("originalPrice", product.getPrice()); // 原价
                item.put("stock", product.getStock());
                item.put("category", product.getCategory());
                
                // 将图片路径转换为完整URL
                String imageUrl = convertToFullUrl(product.getImage());
                item.put("pic", imageUrl);
                item.put("image", imageUrl);
                item.put("minPrice", product.getPrice());
                item.put("status", product.getStatus());
                
                return item;
            })
            .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("total", goodsList.size());
        data.put("pages", 1);
        data.put("limit", 20);
        data.put("page", 1);
        data.put("goods", goodsList);

        result.put("data", data);
        return result;
    }

    @GetMapping("/shop/goods/dynamic")
    @ApiOperation("获取商品动态")
    public Map<String, Object> getGoodsDynamic() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        // 返回模拟的商品动态数据
        List<Map<String, Object>> dynamicList = new java.util.ArrayList<>();

        // 创建一些模拟的商品动态
        Map<String, Object> dynamic1 = new HashMap<>();
        dynamic1.put("nick", "宠物主人");
        dynamic1.put("goodsName", "宠物用品");
        dynamicList.add(dynamic1);

        Map<String, Object> dynamic2 = new HashMap<>();
        dynamic2.put("nick", "宠物爱好者");
        dynamic2.put("goodsName", "宠物食品");
        dynamicList.add(dynamic2);

        Map<String, Object> dynamic3 = new HashMap<>();
        dynamic3.put("nick", "爱宠人士");
        dynamic3.put("goodsName", "宠物玩具");
        dynamicList.add(dynamic3);

        result.put("data", dynamicList);
        return result;
    }

    @GetMapping("/site/adPosition/batch")
    @ApiOperation("批量获取广告位")
    public Map<String, Object> getAdPositionBatch(@RequestParam String keys) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        List<Map<String, Object>> adPositions = new java.util.ArrayList<>();

        String[] keyArray = keys.split(",");
        for (String key : keyArray) {
            Map<String, Object> adPosition = new HashMap<>();
            adPosition.put("id", 1);
            adPosition.put("key", key.trim());
            adPosition.put("name", key.trim());
            adPosition.put("type", "image");
            adPosition.put("url", "/images/banner.jpg");
            adPositions.add(adPosition);
        }

        result.put("data", adPositions);
        return result;
    }

    @GetMapping("/site/goods/dynamic")
    @ApiOperation("获取商品动态")
    public Map<String, Object> getGoodsDynamic(@RequestParam(defaultValue = "0") Integer type) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        List<Map<String, Object>> dynamics = List.of(
            Map.of("id", 1, "type", 0, "goodsId", 1, "content", "新品上架", "createTime", System.currentTimeMillis()),
            Map.of("id", 2, "type", 0, "goodsId", 2, "content", "促销活动", "createTime", System.currentTimeMillis())
        );

        result.put("data", dynamics);
        return result;
    }



    @GetMapping("/card/my")
    @ApiOperation("获取我的卡券")
    public Map<String, Object> getMyCards(@RequestParam(required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        // 返回空列表
        result.put("data", List.of());
        return result;
    }

    @GetMapping("/shopping-cart/info")
    @ApiOperation("获取购物车信息")
    public Map<String, Object> getShoppingCartInfo(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String type) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");

        Map<String, Object> cartInfo = new HashMap<>();
        cartInfo.put("cartTotal", 0);
        cartInfo.put("cartList", List.of());

        result.put("data", cartInfo);
        return result;
    }

    @GetMapping("/user/current")
    @ApiOperation("获取当前用户信息")
    public Map<String, Object> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();

        // 如果没有token，返回默认数据
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> data = new HashMap<>();
            data.put("level", 1);
            data.put("charm", 0);
            data.put("canAmount", 120);
            Map<String, Integer> stats = new HashMap<>();
            stats.put("follows", 0);
            stats.put("fans", 0);
            stats.put("dynamics", 0);
            stats.put("likes", 0);
            data.put("stats", stats);

            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", data);
            return result;
        }

        String token = authHeader.substring(7);
        
        try {
            // 从token中提取用户ID（格式：token_userId_timestamp）
            String[] tokenParts = token.split("_");
            if (tokenParts.length >= 2) {
                Long userId = Long.parseLong(tokenParts[1]);
                
                // 这里可以调用userService获取用户数据，现在返回默认值
                Map<String, Object> data = new HashMap<>();
                data.put("level", 1);
                data.put("charm", 0);
                data.put("canAmount", 120);
                Map<String, Integer> stats = new HashMap<>();
                stats.put("follows", 0);
                stats.put("fans", 0);
                stats.put("dynamics", 0);
                stats.put("likes", 0);
                data.put("stats", stats);

                result.put("code", 0);
                result.put("msg", "success");
                result.put("data", data);
            } else {
                result.put("code", 1);
                result.put("msg", "无效的token格式");
            }
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            result.put("code", 1);
            result.put("msg", "获取用户信息失败");
        }

        return result;
    }
}
