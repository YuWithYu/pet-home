/*
 Navicat Premium Dump SQL

 Source Server         : yu
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : pet_home

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 14/10/2025 22:24:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_default`(`is_default` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of address
-- ----------------------------

-- ----------------------------
-- Table structure for admin_log
-- ----------------------------
DROP TABLE IF EXISTS `admin_log`;
CREATE TABLE `admin_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int NOT NULL,
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `target_id` int NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_action`(`action` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin_log
-- ----------------------------

-- ----------------------------
-- Table structure for ai_recommendation
-- ----------------------------
DROP TABLE IF EXISTS `ai_recommendation`;
CREATE TABLE `ai_recommendation`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `recommendation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` int NULL DEFAULT NULL,
  `score` decimal(5, 2) NULL DEFAULT NULL,
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_recommendation_type`(`recommendation_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_recommendation
-- ----------------------------

-- ----------------------------
-- Table structure for appointment
-- ----------------------------
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `pet_id` int NULL DEFAULT NULL,
  `service_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `appointment_date` datetime NULL DEFAULT NULL,
  `time_slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '???',
  `date` date NULL DEFAULT NULL COMMENT '????',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '????',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '??',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '?????',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `price` decimal(10, 2) NULL DEFAULT 199.00,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '瀹犵墿鍖婚櫌' COMMENT '棰勭害鍦扮偣',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of appointment
-- ----------------------------

-- ----------------------------
-- Table structure for appointment_log
-- ----------------------------
DROP TABLE IF EXISTS `appointment_log`;
CREATE TABLE `appointment_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `appointment_id` int NOT NULL,
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `operator_id` int NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_appointment_id`(`appointment_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of appointment_log
-- ----------------------------

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '轮播图标题',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '轮播图描述',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件访问URL',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序，数字越小越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, '宠物商城活动', '全场八折优惠 限时抢购', '011eec2c-aa29-4fcd-8427-8a2c03655c63.png', '宠物商城活动.png', '/upload/011eec2c-aa29-4fcd-8427-8a2c03655c63.png', 102400, 'image/png', 'active', 1, '2025-10-12 21:56:04', '2025-10-12 22:02:16');
INSERT INTO `banner` VALUES (3, '', '', 'c17e1c86-5174-4411-aa59-54362f8de1ce.png', '生成宠物商城轮播图.png', '/upload/c17e1c86-5174-4411-aa59-54362f8de1ce.png', 4872938, 'image/png', 'active', 0, '2025-10-12 22:18:46', '2025-10-12 22:18:46');

-- ----------------------------
-- Table structure for business_schedule
-- ----------------------------
DROP TABLE IF EXISTS `business_schedule`;
CREATE TABLE `business_schedule`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `service_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `day_of_week` int NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_service_type`(`service_type` ASC) USING BTREE,
  INDEX `idx_day_of_week`(`day_of_week` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of business_schedule
-- ----------------------------

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '分类描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '分类图标路径',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `status` int NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `level` int NULL DEFAULT 1 COMMENT '鍒嗙被灞傜骇锛?-涓?绾у垎绫伙紝2-浜岀骇鍒嗙被',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '宠物服务分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------

-- ----------------------------
-- Table structure for china_region
-- ----------------------------
DROP TABLE IF EXISTS `china_region`;
CREATE TABLE `china_region`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行政区划代码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地区名称',
  `level` int NOT NULL COMMENT '行政级别：1-省级，2-地级，3-县级',
  `parent_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父级行政区划代码',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `status` int NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_parent_code`(`parent_code` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3430 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '中国行政区划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of china_region
-- ----------------------------
INSERT INTO `china_region` VALUES (1, '11', '北京市', 1, NULL, 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (2, '1101', '市辖区', 2, '11', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (3, '110101', '东城区', 3, '1101', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (4, '110102', '西城区', 3, '1101', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (5, '110105', '朝阳区', 3, '1101', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (6, '110106', '丰台区', 3, '1101', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (7, '110107', '石景山区', 3, '1101', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (8, '110108', '海淀区', 3, '1101', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (9, '110109', '门头沟区', 3, '1101', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (10, '110111', '房山区', 3, '1101', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (11, '110112', '通州区', 3, '1101', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (12, '110113', '顺义区', 3, '1101', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (13, '110114', '昌平区', 3, '1101', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (14, '110115', '大兴区', 3, '1101', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (15, '110116', '怀柔区', 3, '1101', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (16, '110117', '平谷区', 3, '1101', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (17, '110118', '密云区', 3, '1101', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (18, '110119', '延庆区', 3, '1101', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (19, '12', '天津市', 1, NULL, 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (20, '1201', '市辖区', 2, '12', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (21, '120101', '和平区', 3, '1201', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (22, '120102', '河东区', 3, '1201', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (23, '120103', '河西区', 3, '1201', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (24, '120104', '南开区', 3, '1201', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (25, '120105', '河北区', 3, '1201', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (26, '120106', '红桥区', 3, '1201', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (27, '120110', '东丽区', 3, '1201', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (28, '120111', '西青区', 3, '1201', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (29, '120112', '津南区', 3, '1201', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (30, '120113', '北辰区', 3, '1201', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (31, '120114', '武清区', 3, '1201', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (32, '120115', '宝坻区', 3, '1201', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (33, '120116', '滨海新区', 3, '1201', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (34, '120117', '宁河区', 3, '1201', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (35, '120118', '静海区', 3, '1201', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (36, '120119', '蓟州区', 3, '1201', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (37, '13', '河北省', 1, NULL, 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (38, '1301', '石家庄市', 2, '13', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (39, '130102', '长安区', 3, '1301', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (40, '130104', '桥西区', 3, '1301', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (41, '130105', '新华区', 3, '1301', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (42, '130107', '井陉矿区', 3, '1301', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (43, '130108', '裕华区', 3, '1301', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (44, '130109', '藁城区', 3, '1301', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (45, '130110', '鹿泉区', 3, '1301', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (46, '130111', '栾城区', 3, '1301', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (47, '130121', '井陉县', 3, '1301', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (48, '130123', '正定县', 3, '1301', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (49, '130125', '行唐县', 3, '1301', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (50, '130126', '灵寿县', 3, '1301', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (51, '130127', '高邑县', 3, '1301', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (52, '130128', '深泽县', 3, '1301', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (53, '130129', '赞皇县', 3, '1301', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (54, '130130', '无极县', 3, '1301', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (55, '130131', '平山县', 3, '1301', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (56, '130132', '元氏县', 3, '1301', 18, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (57, '130133', '赵县', 3, '1301', 19, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (58, '130171', '石家庄高新技术产业开发区', 3, '1301', 20, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (59, '130172', '石家庄循环化工园区', 3, '1301', 21, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (60, '130181', '辛集市', 3, '1301', 22, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (61, '130183', '晋州市', 3, '1301', 23, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (62, '130184', '新乐市', 3, '1301', 24, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (63, '1302', '唐山市', 2, '13', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (64, '130202', '路南区', 3, '1302', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (65, '130203', '路北区', 3, '1302', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (66, '130204', '古冶区', 3, '1302', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (67, '130205', '开平区', 3, '1302', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (68, '130207', '丰南区', 3, '1302', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (69, '130208', '丰润区', 3, '1302', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (70, '130209', '曹妃甸区', 3, '1302', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (71, '130224', '滦南县', 3, '1302', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (72, '130225', '乐亭县', 3, '1302', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (73, '130227', '迁西县', 3, '1302', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (74, '130229', '玉田县', 3, '1302', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (75, '130271', '河北唐山芦台经济开发区', 3, '1302', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (76, '130272', '唐山市汉沽管理区', 3, '1302', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (77, '130273', '唐山高新技术产业开发区', 3, '1302', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (78, '130274', '河北唐山海港经济开发区', 3, '1302', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (79, '130281', '遵化市', 3, '1302', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (80, '130283', '迁安市', 3, '1302', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (81, '130284', '滦州市', 3, '1302', 18, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (82, '1303', '秦皇岛市', 2, '13', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (83, '130302', '海港区', 3, '1303', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (84, '130303', '山海关区', 3, '1303', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (85, '130304', '北戴河区', 3, '1303', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (86, '130306', '抚宁区', 3, '1303', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (87, '130321', '青龙满族自治县', 3, '1303', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (88, '130322', '昌黎县', 3, '1303', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (89, '130324', '卢龙县', 3, '1303', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (90, '130371', '秦皇岛市经济技术开发区', 3, '1303', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (91, '130372', '北戴河新区', 3, '1303', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (92, '1304', '邯郸市', 2, '13', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (93, '130402', '邯山区', 3, '1304', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (94, '130403', '丛台区', 3, '1304', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (95, '130404', '复兴区', 3, '1304', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (96, '130406', '峰峰矿区', 3, '1304', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (97, '130407', '肥乡区', 3, '1304', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (98, '130408', '永年区', 3, '1304', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (99, '130423', '临漳县', 3, '1304', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (100, '130424', '成安县', 3, '1304', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (101, '130425', '大名县', 3, '1304', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (102, '130426', '涉县', 3, '1304', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (103, '130427', '磁县', 3, '1304', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (104, '130430', '邱县', 3, '1304', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (105, '130431', '鸡泽县', 3, '1304', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (106, '130432', '广平县', 3, '1304', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (107, '130433', '馆陶县', 3, '1304', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (108, '130434', '魏县', 3, '1304', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (109, '130435', '曲周县', 3, '1304', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (110, '130471', '邯郸经济技术开发区', 3, '1304', 18, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (111, '130473', '邯郸冀南新区', 3, '1304', 19, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (112, '130481', '武安市', 3, '1304', 20, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (113, '1305', '邢台市', 2, '13', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (114, '130502', '襄都区', 3, '1305', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (115, '130503', '信都区', 3, '1305', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (116, '130505', '任泽区', 3, '1305', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (117, '130506', '南和区', 3, '1305', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (118, '130522', '临城县', 3, '1305', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (119, '130523', '内丘县', 3, '1305', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (120, '130524', '柏乡县', 3, '1305', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (121, '130525', '隆尧县', 3, '1305', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (122, '130528', '宁晋县', 3, '1305', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (123, '130529', '巨鹿县', 3, '1305', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (124, '130530', '新河县', 3, '1305', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (125, '130531', '广宗县', 3, '1305', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (126, '130532', '平乡县', 3, '1305', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (127, '130533', '威县', 3, '1305', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (128, '130534', '清河县', 3, '1305', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (129, '130535', '临西县', 3, '1305', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (130, '130571', '河北邢台经济开发区', 3, '1305', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (131, '130581', '南宫市', 3, '1305', 18, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (132, '130582', '沙河市', 3, '1305', 19, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (133, '1306', '保定市', 2, '13', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (134, '130602', '竞秀区', 3, '1306', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (135, '130606', '莲池区', 3, '1306', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (136, '130607', '满城区', 3, '1306', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (137, '130608', '清苑区', 3, '1306', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (138, '130609', '徐水区', 3, '1306', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (139, '130623', '涞水县', 3, '1306', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (140, '130624', '阜平县', 3, '1306', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (141, '130626', '定兴县', 3, '1306', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (142, '130627', '唐县', 3, '1306', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (143, '130628', '高阳县', 3, '1306', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (144, '130629', '容城县', 3, '1306', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (145, '130630', '涞源县', 3, '1306', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (146, '130631', '望都县', 3, '1306', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (147, '130632', '安新县', 3, '1306', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (148, '130633', '易县', 3, '1306', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (149, '130634', '曲阳县', 3, '1306', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (150, '130635', '蠡县', 3, '1306', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (151, '130636', '顺平县', 3, '1306', 18, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (152, '130637', '博野县', 3, '1306', 19, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (153, '130638', '雄县', 3, '1306', 20, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (154, '130671', '保定高新技术产业开发区', 3, '1306', 21, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (155, '130672', '保定白沟新城', 3, '1306', 22, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (156, '130681', '涿州市', 3, '1306', 23, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (157, '130682', '定州市', 3, '1306', 24, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (158, '130683', '安国市', 3, '1306', 25, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (159, '130684', '高碑店市', 3, '1306', 26, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (160, '1307', '张家口市', 2, '13', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (161, '130702', '桥东区', 3, '1307', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (162, '130703', '桥西区', 3, '1307', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (163, '130705', '宣化区', 3, '1307', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (164, '130706', '下花园区', 3, '1307', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (165, '130708', '万全区', 3, '1307', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (166, '130709', '崇礼区', 3, '1307', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (167, '130722', '张北县', 3, '1307', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (168, '130723', '康保县', 3, '1307', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (169, '130724', '沽源县', 3, '1307', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (170, '130725', '尚义县', 3, '1307', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (171, '130726', '蔚县', 3, '1307', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (172, '130727', '阳原县', 3, '1307', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (173, '130728', '怀安县', 3, '1307', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (174, '130730', '怀来县', 3, '1307', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (175, '130731', '涿鹿县', 3, '1307', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (176, '130732', '赤城县', 3, '1307', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (177, '130771', '张家口经济开发区', 3, '1307', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (178, '130772', '张家口市察北管理区', 3, '1307', 18, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (179, '130773', '张家口市塞北管理区', 3, '1307', 19, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (180, '1308', '承德市', 2, '13', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (181, '130802', '双桥区', 3, '1308', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (182, '130803', '双滦区', 3, '1308', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (183, '130804', '鹰手营子矿区', 3, '1308', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (184, '130821', '承德县', 3, '1308', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (185, '130822', '兴隆县', 3, '1308', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (186, '130824', '滦平县', 3, '1308', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (187, '130825', '隆化县', 3, '1308', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (188, '130826', '丰宁满族自治县', 3, '1308', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (189, '130827', '宽城满族自治县', 3, '1308', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (190, '130828', '围场满族蒙古族自治县', 3, '1308', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (191, '130871', '承德高新技术产业开发区', 3, '1308', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (192, '130881', '平泉市', 3, '1308', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (193, '1309', '沧州市', 2, '13', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (194, '130902', '新华区', 3, '1309', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (195, '130903', '运河区', 3, '1309', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (196, '130921', '沧县', 3, '1309', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (197, '130922', '青县', 3, '1309', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (198, '130923', '东光县', 3, '1309', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (199, '130924', '海兴县', 3, '1309', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (200, '130925', '盐山县', 3, '1309', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (201, '130926', '肃宁县', 3, '1309', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (202, '130927', '南皮县', 3, '1309', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (203, '130928', '吴桥县', 3, '1309', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (204, '130929', '献县', 3, '1309', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (205, '130930', '孟村回族自治县', 3, '1309', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (206, '130971', '河北沧州经济开发区', 3, '1309', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (207, '130972', '沧州高新技术产业开发区', 3, '1309', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (208, '130973', '沧州渤海新区', 3, '1309', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (209, '130981', '泊头市', 3, '1309', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (210, '130982', '任丘市', 3, '1309', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (211, '130983', '黄骅市', 3, '1309', 18, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (212, '130984', '河间市', 3, '1309', 19, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (213, '1310', '廊坊市', 2, '13', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (214, '131002', '安次区', 3, '1310', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (215, '131003', '广阳区', 3, '1310', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (216, '131022', '固安县', 3, '1310', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (217, '131023', '永清县', 3, '1310', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (218, '131024', '香河县', 3, '1310', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (219, '131025', '大城县', 3, '1310', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (220, '131026', '文安县', 3, '1310', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (221, '131028', '大厂回族自治县', 3, '1310', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (222, '131071', '廊坊经济技术开发区', 3, '1310', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (223, '131081', '霸州市', 3, '1310', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (224, '131082', '三河市', 3, '1310', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (225, '1311', '衡水市', 2, '13', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (226, '131102', '桃城区', 3, '1311', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (227, '131103', '冀州区', 3, '1311', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (228, '131121', '枣强县', 3, '1311', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (229, '131122', '武邑县', 3, '1311', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (230, '131123', '武强县', 3, '1311', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (231, '131124', '饶阳县', 3, '1311', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (232, '131125', '安平县', 3, '1311', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (233, '131126', '故城县', 3, '1311', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (234, '131127', '景县', 3, '1311', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (235, '131128', '阜城县', 3, '1311', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (236, '131171', '河北衡水高新技术产业开发区', 3, '1311', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (237, '131172', '衡水滨湖新区', 3, '1311', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (238, '131182', '深州市', 3, '1311', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (239, '14', '山西省', 1, NULL, 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (240, '1401', '太原市', 2, '14', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (241, '140105', '小店区', 3, '1401', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (242, '140106', '迎泽区', 3, '1401', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (243, '140107', '杏花岭区', 3, '1401', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (244, '140108', '尖草坪区', 3, '1401', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (245, '140109', '万柏林区', 3, '1401', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (246, '140110', '晋源区', 3, '1401', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (247, '140121', '清徐县', 3, '1401', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (248, '140122', '阳曲县', 3, '1401', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (249, '140123', '娄烦县', 3, '1401', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (250, '140171', '山西转型综合改革示范区', 3, '1401', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (251, '140181', '古交市', 3, '1401', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (252, '1402', '大同市', 2, '14', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (253, '140212', '新荣区', 3, '1402', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (254, '140213', '平城区', 3, '1402', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (255, '140214', '云冈区', 3, '1402', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (256, '140215', '云州区', 3, '1402', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (257, '140221', '阳高县', 3, '1402', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (258, '140222', '天镇县', 3, '1402', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (259, '140223', '广灵县', 3, '1402', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (260, '140224', '灵丘县', 3, '1402', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (261, '140225', '浑源县', 3, '1402', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (262, '140226', '左云县', 3, '1402', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (263, '140271', '山西大同经济开发区', 3, '1402', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (264, '1403', '阳泉市', 2, '14', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (265, '140302', '城区', 3, '1403', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (266, '140303', '矿区', 3, '1403', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (267, '140311', '郊区', 3, '1403', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (268, '140321', '平定县', 3, '1403', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (269, '140322', '盂县', 3, '1403', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (270, '1404', '长治市', 2, '14', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (271, '140403', '潞州区', 3, '1404', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (272, '140404', '上党区', 3, '1404', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (273, '140405', '屯留区', 3, '1404', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (274, '140406', '潞城区', 3, '1404', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (275, '140423', '襄垣县', 3, '1404', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (276, '140425', '平顺县', 3, '1404', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (277, '140426', '黎城县', 3, '1404', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (278, '140427', '壶关县', 3, '1404', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (279, '140428', '长子县', 3, '1404', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (280, '140429', '武乡县', 3, '1404', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (281, '140430', '沁县', 3, '1404', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (282, '140431', '沁源县', 3, '1404', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (283, '1405', '晋城市', 2, '14', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (284, '140502', '城区', 3, '1405', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (285, '140521', '沁水县', 3, '1405', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (286, '140522', '阳城县', 3, '1405', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (287, '140524', '陵川县', 3, '1405', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (288, '140525', '泽州县', 3, '1405', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (289, '140581', '高平市', 3, '1405', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (290, '1406', '朔州市', 2, '14', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (291, '140602', '朔城区', 3, '1406', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (292, '140603', '平鲁区', 3, '1406', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (293, '140621', '山阴县', 3, '1406', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (294, '140622', '应县', 3, '1406', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (295, '140623', '右玉县', 3, '1406', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (296, '140671', '山西朔州经济开发区', 3, '1406', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (297, '140681', '怀仁市', 3, '1406', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (298, '1407', '晋中市', 2, '14', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (299, '140702', '榆次区', 3, '1407', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (300, '140703', '太谷区', 3, '1407', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (301, '140721', '榆社县', 3, '1407', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (302, '140722', '左权县', 3, '1407', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (303, '140723', '和顺县', 3, '1407', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (304, '140724', '昔阳县', 3, '1407', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (305, '140725', '寿阳县', 3, '1407', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (306, '140727', '祁县', 3, '1407', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (307, '140728', '平遥县', 3, '1407', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (308, '140729', '灵石县', 3, '1407', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (309, '140781', '介休市', 3, '1407', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (310, '1408', '运城市', 2, '14', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (311, '140802', '盐湖区', 3, '1408', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (312, '140821', '临猗县', 3, '1408', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (313, '140822', '万荣县', 3, '1408', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (314, '140823', '闻喜县', 3, '1408', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (315, '140824', '稷山县', 3, '1408', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (316, '140825', '新绛县', 3, '1408', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (317, '140826', '绛县', 3, '1408', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (318, '140827', '垣曲县', 3, '1408', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (319, '140828', '夏县', 3, '1408', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (320, '140829', '平陆县', 3, '1408', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (321, '140830', '芮城县', 3, '1408', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (322, '140881', '永济市', 3, '1408', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (323, '140882', '河津市', 3, '1408', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (324, '1409', '忻州市', 2, '14', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (325, '140902', '忻府区', 3, '1409', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (326, '140921', '定襄县', 3, '1409', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (327, '140922', '五台县', 3, '1409', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (328, '140923', '代县', 3, '1409', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (329, '140924', '繁峙县', 3, '1409', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (330, '140925', '宁武县', 3, '1409', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (331, '140926', '静乐县', 3, '1409', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (332, '140927', '神池县', 3, '1409', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (333, '140928', '五寨县', 3, '1409', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (334, '140929', '岢岚县', 3, '1409', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (335, '140930', '河曲县', 3, '1409', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (336, '140931', '保德县', 3, '1409', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (337, '140932', '偏关县', 3, '1409', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (338, '140971', '五台山风景名胜区', 3, '1409', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (339, '140981', '原平市', 3, '1409', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (340, '1410', '临汾市', 2, '14', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (341, '141002', '尧都区', 3, '1410', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (342, '141021', '曲沃县', 3, '1410', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (343, '141022', '翼城县', 3, '1410', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (344, '141023', '襄汾县', 3, '1410', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (345, '141024', '洪洞县', 3, '1410', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (346, '141025', '古县', 3, '1410', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (347, '141026', '安泽县', 3, '1410', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (348, '141027', '浮山县', 3, '1410', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (349, '141028', '吉县', 3, '1410', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (350, '141029', '乡宁县', 3, '1410', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (351, '141030', '大宁县', 3, '1410', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (352, '141031', '隰县', 3, '1410', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (353, '141032', '永和县', 3, '1410', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (354, '141033', '蒲县', 3, '1410', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (355, '141034', '汾西县', 3, '1410', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (356, '141081', '侯马市', 3, '1410', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (357, '141082', '霍州市', 3, '1410', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (358, '1411', '吕梁市', 2, '14', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (359, '141102', '离石区', 3, '1411', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (360, '141121', '文水县', 3, '1411', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (361, '141122', '交城县', 3, '1411', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (362, '141123', '兴县', 3, '1411', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (363, '141124', '临县', 3, '1411', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (364, '141125', '柳林县', 3, '1411', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (365, '141126', '石楼县', 3, '1411', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (366, '141127', '岚县', 3, '1411', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (367, '141128', '方山县', 3, '1411', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (368, '141129', '中阳县', 3, '1411', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (369, '141130', '交口县', 3, '1411', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (370, '141181', '孝义市', 3, '1411', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (371, '141182', '汾阳市', 3, '1411', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (372, '15', '内蒙古自治区', 1, NULL, 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (373, '1501', '呼和浩特市', 2, '15', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (374, '150102', '新城区', 3, '1501', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (375, '150103', '回民区', 3, '1501', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (376, '150104', '玉泉区', 3, '1501', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (377, '150105', '赛罕区', 3, '1501', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (378, '150121', '土默特左旗', 3, '1501', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (379, '150122', '托克托县', 3, '1501', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (380, '150123', '和林格尔县', 3, '1501', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (381, '150124', '清水河县', 3, '1501', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (382, '150125', '武川县', 3, '1501', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (383, '150172', '呼和浩特经济技术开发区', 3, '1501', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (384, '1502', '包头市', 2, '15', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (385, '150202', '东河区', 3, '1502', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (386, '150203', '昆都仑区', 3, '1502', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (387, '150204', '青山区', 3, '1502', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (388, '150205', '石拐区', 3, '1502', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (389, '150206', '白云鄂博矿区', 3, '1502', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (390, '150207', '九原区', 3, '1502', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (391, '150221', '土默特右旗', 3, '1502', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (392, '150222', '固阳县', 3, '1502', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (393, '150223', '达尔罕茂明安联合旗', 3, '1502', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (394, '150271', '包头稀土高新技术产业开发区', 3, '1502', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (395, '1503', '乌海市', 2, '15', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (396, '150302', '海勃湾区', 3, '1503', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (397, '150303', '海南区', 3, '1503', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (398, '150304', '乌达区', 3, '1503', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (399, '1504', '赤峰市', 2, '15', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (400, '150402', '红山区', 3, '1504', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (401, '150403', '元宝山区', 3, '1504', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (402, '150404', '松山区', 3, '1504', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (403, '150421', '阿鲁科尔沁旗', 3, '1504', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (404, '150422', '巴林左旗', 3, '1504', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (405, '150423', '巴林右旗', 3, '1504', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (406, '150424', '林西县', 3, '1504', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (407, '150425', '克什克腾旗', 3, '1504', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (408, '150426', '翁牛特旗', 3, '1504', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (409, '150428', '喀喇沁旗', 3, '1504', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (410, '150429', '宁城县', 3, '1504', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (411, '150430', '敖汉旗', 3, '1504', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (412, '1505', '通辽市', 2, '15', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (413, '150502', '科尔沁区', 3, '1505', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (414, '150521', '科尔沁左翼中旗', 3, '1505', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (415, '150522', '科尔沁左翼后旗', 3, '1505', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (416, '150523', '开鲁县', 3, '1505', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (417, '150524', '库伦旗', 3, '1505', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (418, '150525', '奈曼旗', 3, '1505', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (419, '150526', '扎鲁特旗', 3, '1505', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (420, '150571', '通辽经济技术开发区', 3, '1505', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (421, '150581', '霍林郭勒市', 3, '1505', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (422, '1506', '鄂尔多斯市', 2, '15', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (423, '150602', '东胜区', 3, '1506', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (424, '150603', '康巴什区', 3, '1506', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (425, '150621', '达拉特旗', 3, '1506', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (426, '150622', '准格尔旗', 3, '1506', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (427, '150623', '鄂托克前旗', 3, '1506', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (428, '150624', '鄂托克旗', 3, '1506', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (429, '150625', '杭锦旗', 3, '1506', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (430, '150626', '乌审旗', 3, '1506', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (431, '150627', '伊金霍洛旗', 3, '1506', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (432, '1507', '呼伦贝尔市', 2, '15', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (433, '150702', '海拉尔区', 3, '1507', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (434, '150703', '扎赉诺尔区', 3, '1507', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (435, '150721', '阿荣旗', 3, '1507', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (436, '150722', '莫力达瓦达斡尔族自治旗', 3, '1507', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (437, '150723', '鄂伦春自治旗', 3, '1507', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (438, '150724', '鄂温克族自治旗', 3, '1507', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (439, '150725', '陈巴尔虎旗', 3, '1507', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (440, '150726', '新巴尔虎左旗', 3, '1507', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (441, '150727', '新巴尔虎右旗', 3, '1507', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (442, '150781', '满洲里市', 3, '1507', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (443, '150782', '牙克石市', 3, '1507', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (444, '150783', '扎兰屯市', 3, '1507', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (445, '150784', '额尔古纳市', 3, '1507', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (446, '150785', '根河市', 3, '1507', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (447, '1508', '巴彦淖尔市', 2, '15', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (448, '150802', '临河区', 3, '1508', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (449, '150821', '五原县', 3, '1508', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (450, '150822', '磴口县', 3, '1508', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (451, '150823', '乌拉特前旗', 3, '1508', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (452, '150824', '乌拉特中旗', 3, '1508', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (453, '150825', '乌拉特后旗', 3, '1508', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (454, '150826', '杭锦后旗', 3, '1508', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (455, '1509', '乌兰察布市', 2, '15', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (456, '150902', '集宁区', 3, '1509', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (457, '150921', '卓资县', 3, '1509', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (458, '150922', '化德县', 3, '1509', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (459, '150923', '商都县', 3, '1509', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (460, '150924', '兴和县', 3, '1509', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (461, '150925', '凉城县', 3, '1509', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (462, '150926', '察哈尔右翼前旗', 3, '1509', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (463, '150927', '察哈尔右翼中旗', 3, '1509', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (464, '150928', '察哈尔右翼后旗', 3, '1509', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (465, '150929', '四子王旗', 3, '1509', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (466, '150981', '丰镇市', 3, '1509', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (467, '1522', '兴安盟', 2, '15', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (468, '152201', '乌兰浩特市', 3, '1522', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (469, '152202', '阿尔山市', 3, '1522', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (470, '152221', '科尔沁右翼前旗', 3, '1522', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (471, '152222', '科尔沁右翼中旗', 3, '1522', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (472, '152223', '扎赉特旗', 3, '1522', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (473, '152224', '突泉县', 3, '1522', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (474, '1525', '锡林郭勒盟', 2, '15', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (475, '152501', '二连浩特市', 3, '1525', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (476, '152502', '锡林浩特市', 3, '1525', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (477, '152522', '阿巴嘎旗', 3, '1525', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (478, '152523', '苏尼特左旗', 3, '1525', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (479, '152524', '苏尼特右旗', 3, '1525', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (480, '152525', '东乌珠穆沁旗', 3, '1525', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (481, '152526', '西乌珠穆沁旗', 3, '1525', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (482, '152527', '太仆寺旗', 3, '1525', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (483, '152528', '镶黄旗', 3, '1525', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (484, '152529', '正镶白旗', 3, '1525', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (485, '152530', '正蓝旗', 3, '1525', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (486, '152531', '多伦县', 3, '1525', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (487, '152571', '乌拉盖管理区管委会', 3, '1525', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (488, '1529', '阿拉善盟', 2, '15', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (489, '152921', '阿拉善左旗', 3, '1529', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (490, '152922', '阿拉善右旗', 3, '1529', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (491, '152923', '额济纳旗', 3, '1529', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (492, '152971', '内蒙古阿拉善高新技术产业开发区', 3, '1529', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (493, '21', '辽宁省', 1, NULL, 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (494, '2101', '沈阳市', 2, '21', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (495, '210102', '和平区', 3, '2101', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (496, '210103', '沈河区', 3, '2101', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (497, '210104', '大东区', 3, '2101', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (498, '210105', '皇姑区', 3, '2101', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (499, '210106', '铁西区', 3, '2101', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (500, '210111', '苏家屯区', 3, '2101', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (501, '210112', '浑南区', 3, '2101', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (502, '210113', '沈北新区', 3, '2101', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (503, '210114', '于洪区', 3, '2101', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (504, '210115', '辽中区', 3, '2101', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (505, '210123', '康平县', 3, '2101', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (506, '210124', '法库县', 3, '2101', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (507, '210181', '新民市', 3, '2101', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (508, '2102', '大连市', 2, '21', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (509, '210202', '中山区', 3, '2102', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (510, '210203', '西岗区', 3, '2102', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (511, '210204', '沙河口区', 3, '2102', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (512, '210211', '甘井子区', 3, '2102', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (513, '210212', '旅顺口区', 3, '2102', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (514, '210213', '金州区', 3, '2102', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (515, '210214', '普兰店区', 3, '2102', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (516, '210224', '长海县', 3, '2102', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (517, '210281', '瓦房店市', 3, '2102', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (518, '210283', '庄河市', 3, '2102', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (519, '2103', '鞍山市', 2, '21', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (520, '210302', '铁东区', 3, '2103', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (521, '210303', '铁西区', 3, '2103', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (522, '210304', '立山区', 3, '2103', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (523, '210311', '千山区', 3, '2103', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (524, '210321', '台安县', 3, '2103', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (525, '210323', '岫岩满族自治县', 3, '2103', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (526, '210381', '海城市', 3, '2103', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (527, '2104', '抚顺市', 2, '21', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (528, '210402', '新抚区', 3, '2104', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (529, '210403', '东洲区', 3, '2104', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (530, '210404', '望花区', 3, '2104', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (531, '210411', '顺城区', 3, '2104', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (532, '210421', '抚顺县', 3, '2104', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (533, '210422', '新宾满族自治县', 3, '2104', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (534, '210423', '清原满族自治县', 3, '2104', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (535, '2105', '本溪市', 2, '21', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (536, '210502', '平山区', 3, '2105', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (537, '210503', '溪湖区', 3, '2105', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (538, '210504', '明山区', 3, '2105', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (539, '210505', '南芬区', 3, '2105', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (540, '210521', '本溪满族自治县', 3, '2105', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (541, '210522', '桓仁满族自治县', 3, '2105', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (542, '2106', '丹东市', 2, '21', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (543, '210602', '元宝区', 3, '2106', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (544, '210603', '振兴区', 3, '2106', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (545, '210604', '振安区', 3, '2106', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (546, '210624', '宽甸满族自治县', 3, '2106', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (547, '210681', '东港市', 3, '2106', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (548, '210682', '凤城市', 3, '2106', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (549, '2107', '锦州市', 2, '21', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (550, '210702', '古塔区', 3, '2107', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (551, '210703', '凌河区', 3, '2107', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (552, '210711', '太和区', 3, '2107', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (553, '210726', '黑山县', 3, '2107', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (554, '210727', '义县', 3, '2107', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (555, '210781', '凌海市', 3, '2107', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (556, '210782', '北镇市', 3, '2107', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (557, '2108', '营口市', 2, '21', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (558, '210802', '站前区', 3, '2108', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (559, '210803', '西市区', 3, '2108', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (560, '210804', '鲅鱼圈区', 3, '2108', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (561, '210811', '老边区', 3, '2108', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (562, '210881', '盖州市', 3, '2108', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (563, '210882', '大石桥市', 3, '2108', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (564, '2109', '阜新市', 2, '21', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (565, '210902', '海州区', 3, '2109', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (566, '210903', '新邱区', 3, '2109', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (567, '210904', '太平区', 3, '2109', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (568, '210905', '清河门区', 3, '2109', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (569, '210911', '细河区', 3, '2109', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (570, '210921', '阜新蒙古族自治县', 3, '2109', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (571, '210922', '彰武县', 3, '2109', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (572, '2110', '辽阳市', 2, '21', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (573, '211002', '白塔区', 3, '2110', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (574, '211003', '文圣区', 3, '2110', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (575, '211004', '宏伟区', 3, '2110', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (576, '211005', '弓长岭区', 3, '2110', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (577, '211011', '太子河区', 3, '2110', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (578, '211021', '辽阳县', 3, '2110', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (579, '211081', '灯塔市', 3, '2110', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (580, '2111', '盘锦市', 2, '21', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (581, '211102', '双台子区', 3, '2111', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (582, '211103', '兴隆台区', 3, '2111', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (583, '211104', '大洼区', 3, '2111', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (584, '211122', '盘山县', 3, '2111', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (585, '2112', '铁岭市', 2, '21', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (586, '211202', '银州区', 3, '2112', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (587, '211204', '清河区', 3, '2112', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (588, '211221', '铁岭县', 3, '2112', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (589, '211223', '西丰县', 3, '2112', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (590, '211224', '昌图县', 3, '2112', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (591, '211281', '调兵山市', 3, '2112', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (592, '211282', '开原市', 3, '2112', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (593, '2113', '朝阳市', 2, '21', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (594, '211302', '双塔区', 3, '2113', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (595, '211303', '龙城区', 3, '2113', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (596, '211321', '朝阳县', 3, '2113', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (597, '211322', '建平县', 3, '2113', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (598, '211324', '喀喇沁左翼蒙古族自治县', 3, '2113', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (599, '211381', '北票市', 3, '2113', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (600, '211382', '凌源市', 3, '2113', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (601, '2114', '葫芦岛市', 2, '21', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (602, '211402', '连山区', 3, '2114', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (603, '211403', '龙港区', 3, '2114', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (604, '211404', '南票区', 3, '2114', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (605, '211421', '绥中县', 3, '2114', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (606, '211422', '建昌县', 3, '2114', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (607, '211481', '兴城市', 3, '2114', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (608, '22', '吉林省', 1, NULL, 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (609, '2201', '长春市', 2, '22', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (610, '220102', '南关区', 3, '2201', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (611, '220103', '宽城区', 3, '2201', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (612, '220104', '朝阳区', 3, '2201', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (613, '220105', '二道区', 3, '2201', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (614, '220106', '绿园区', 3, '2201', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (615, '220112', '双阳区', 3, '2201', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (616, '220113', '九台区', 3, '2201', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (617, '220122', '农安县', 3, '2201', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (618, '220171', '长春经济技术开发区', 3, '2201', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (619, '220172', '长春净月高新技术产业开发区', 3, '2201', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (620, '220173', '长春高新技术产业开发区', 3, '2201', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (621, '220174', '长春汽车经济技术开发区', 3, '2201', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (622, '220182', '榆树市', 3, '2201', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (623, '220183', '德惠市', 3, '2201', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (624, '220184', '公主岭市', 3, '2201', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (625, '2202', '吉林市', 2, '22', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (626, '220202', '昌邑区', 3, '2202', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (627, '220203', '龙潭区', 3, '2202', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (628, '220204', '船营区', 3, '2202', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (629, '220211', '丰满区', 3, '2202', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (630, '220221', '永吉县', 3, '2202', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (631, '220271', '吉林经济开发区', 3, '2202', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (632, '220272', '吉林高新技术产业开发区', 3, '2202', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (633, '220273', '吉林中国新加坡食品区', 3, '2202', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (634, '220281', '蛟河市', 3, '2202', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (635, '220282', '桦甸市', 3, '2202', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (636, '220283', '舒兰市', 3, '2202', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (637, '220284', '磐石市', 3, '2202', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (638, '2203', '四平市', 2, '22', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (639, '220302', '铁西区', 3, '2203', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (640, '220303', '铁东区', 3, '2203', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (641, '220322', '梨树县', 3, '2203', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (642, '220323', '伊通满族自治县', 3, '2203', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (643, '220382', '双辽市', 3, '2203', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (644, '2204', '辽源市', 2, '22', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (645, '220402', '龙山区', 3, '2204', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (646, '220403', '西安区', 3, '2204', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (647, '220421', '东丰县', 3, '2204', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (648, '220422', '东辽县', 3, '2204', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (649, '2205', '通化市', 2, '22', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (650, '220502', '东昌区', 3, '2205', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (651, '220503', '二道江区', 3, '2205', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (652, '220521', '通化县', 3, '2205', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (653, '220523', '辉南县', 3, '2205', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (654, '220524', '柳河县', 3, '2205', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (655, '220581', '梅河口市', 3, '2205', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (656, '220582', '集安市', 3, '2205', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (657, '2206', '白山市', 2, '22', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (658, '220602', '浑江区', 3, '2206', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (659, '220605', '江源区', 3, '2206', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (660, '220621', '抚松县', 3, '2206', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (661, '220622', '靖宇县', 3, '2206', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (662, '220623', '长白朝鲜族自治县', 3, '2206', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (663, '220681', '临江市', 3, '2206', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (664, '2207', '松原市', 2, '22', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (665, '220702', '宁江区', 3, '2207', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (666, '220721', '前郭尔罗斯蒙古族自治县', 3, '2207', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (667, '220722', '长岭县', 3, '2207', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (668, '220723', '乾安县', 3, '2207', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (669, '220771', '吉林松原经济开发区', 3, '2207', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (670, '220781', '扶余市', 3, '2207', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (671, '2208', '白城市', 2, '22', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (672, '220802', '洮北区', 3, '2208', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (673, '220821', '镇赉县', 3, '2208', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (674, '220822', '通榆县', 3, '2208', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (675, '220871', '吉林白城经济开发区', 3, '2208', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (676, '220881', '洮南市', 3, '2208', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (677, '220882', '大安市', 3, '2208', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (678, '2224', '延边朝鲜族自治州', 2, '22', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (679, '222401', '延吉市', 3, '2224', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (680, '222402', '图们市', 3, '2224', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (681, '222403', '敦化市', 3, '2224', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (682, '222404', '珲春市', 3, '2224', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (683, '222405', '龙井市', 3, '2224', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (684, '222406', '和龙市', 3, '2224', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (685, '222424', '汪清县', 3, '2224', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (686, '222426', '安图县', 3, '2224', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (687, '23', '黑龙江省', 1, NULL, 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (688, '2301', '哈尔滨市', 2, '23', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (689, '230102', '道里区', 3, '2301', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (690, '230103', '南岗区', 3, '2301', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (691, '230104', '道外区', 3, '2301', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (692, '230108', '平房区', 3, '2301', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (693, '230109', '松北区', 3, '2301', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (694, '230110', '香坊区', 3, '2301', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (695, '230111', '呼兰区', 3, '2301', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (696, '230112', '阿城区', 3, '2301', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (697, '230113', '双城区', 3, '2301', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (698, '230123', '依兰县', 3, '2301', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (699, '230124', '方正县', 3, '2301', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (700, '230125', '宾县', 3, '2301', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (701, '230126', '巴彦县', 3, '2301', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (702, '230127', '木兰县', 3, '2301', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (703, '230128', '通河县', 3, '2301', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (704, '230129', '延寿县', 3, '2301', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (705, '230183', '尚志市', 3, '2301', 17, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (706, '230184', '五常市', 3, '2301', 18, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (707, '2302', '齐齐哈尔市', 2, '23', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (708, '230202', '龙沙区', 3, '2302', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (709, '230203', '建华区', 3, '2302', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (710, '230204', '铁锋区', 3, '2302', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (711, '230205', '昂昂溪区', 3, '2302', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (712, '230206', '富拉尔基区', 3, '2302', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (713, '230207', '碾子山区', 3, '2302', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (714, '230208', '梅里斯达斡尔族区', 3, '2302', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (715, '230221', '龙江县', 3, '2302', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (716, '230223', '依安县', 3, '2302', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (717, '230224', '泰来县', 3, '2302', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (718, '230225', '甘南县', 3, '2302', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (719, '230227', '富裕县', 3, '2302', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (720, '230229', '克山县', 3, '2302', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (721, '230230', '克东县', 3, '2302', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (722, '230231', '拜泉县', 3, '2302', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (723, '230281', '讷河市', 3, '2302', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (724, '2303', '鸡西市', 2, '23', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (725, '230302', '鸡冠区', 3, '2303', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (726, '230303', '恒山区', 3, '2303', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (727, '230304', '滴道区', 3, '2303', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (728, '230305', '梨树区', 3, '2303', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (729, '230306', '城子河区', 3, '2303', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (730, '230307', '麻山区', 3, '2303', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (731, '230321', '鸡东县', 3, '2303', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (732, '230381', '虎林市', 3, '2303', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (733, '230382', '密山市', 3, '2303', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (734, '2304', '鹤岗市', 2, '23', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (735, '230402', '向阳区', 3, '2304', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (736, '230403', '工农区', 3, '2304', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (737, '230404', '南山区', 3, '2304', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (738, '230405', '兴安区', 3, '2304', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (739, '230406', '东山区', 3, '2304', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (740, '230407', '兴山区', 3, '2304', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (741, '230421', '萝北县', 3, '2304', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (742, '230422', '绥滨县', 3, '2304', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (743, '2305', '双鸭山市', 2, '23', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (744, '230502', '尖山区', 3, '2305', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (745, '230503', '岭东区', 3, '2305', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (746, '230505', '四方台区', 3, '2305', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (747, '230506', '宝山区', 3, '2305', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (748, '230521', '集贤县', 3, '2305', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (749, '230522', '友谊县', 3, '2305', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (750, '230523', '宝清县', 3, '2305', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (751, '230524', '饶河县', 3, '2305', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (752, '2306', '大庆市', 2, '23', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (753, '230602', '萨尔图区', 3, '2306', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (754, '230603', '龙凤区', 3, '2306', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (755, '230604', '让胡路区', 3, '2306', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (756, '230605', '红岗区', 3, '2306', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (757, '230606', '大同区', 3, '2306', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (758, '230621', '肇州县', 3, '2306', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (759, '230622', '肇源县', 3, '2306', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (760, '230623', '林甸县', 3, '2306', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (761, '230624', '杜尔伯特蒙古族自治县', 3, '2306', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (762, '230671', '大庆高新技术产业开发区', 3, '2306', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (763, '2307', '伊春市', 2, '23', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (764, '230717', '伊美区', 3, '2307', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (765, '230718', '乌翠区', 3, '2307', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (766, '230719', '友好区', 3, '2307', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (767, '230722', '嘉荫县', 3, '2307', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (768, '230723', '汤旺县', 3, '2307', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (769, '230724', '丰林县', 3, '2307', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (770, '230725', '大箐山县', 3, '2307', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (771, '230726', '南岔县', 3, '2307', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (772, '230751', '金林区', 3, '2307', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (773, '230781', '铁力市', 3, '2307', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (774, '2308', '佳木斯市', 2, '23', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (775, '230803', '向阳区', 3, '2308', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (776, '230804', '前进区', 3, '2308', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (777, '230805', '东风区', 3, '2308', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (778, '230811', '郊区', 3, '2308', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (779, '230822', '桦南县', 3, '2308', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (780, '230826', '桦川县', 3, '2308', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (781, '230828', '汤原县', 3, '2308', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (782, '230881', '同江市', 3, '2308', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (783, '230882', '富锦市', 3, '2308', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (784, '230883', '抚远市', 3, '2308', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (785, '2309', '七台河市', 2, '23', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (786, '230902', '新兴区', 3, '2309', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (787, '230903', '桃山区', 3, '2309', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (788, '230904', '茄子河区', 3, '2309', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (789, '230921', '勃利县', 3, '2309', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (790, '2310', '牡丹江市', 2, '23', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (791, '231002', '东安区', 3, '2310', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (792, '231003', '阳明区', 3, '2310', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (793, '231004', '爱民区', 3, '2310', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (794, '231005', '西安区', 3, '2310', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (795, '231025', '林口县', 3, '2310', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (796, '231081', '绥芬河市', 3, '2310', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (797, '231083', '海林市', 3, '2310', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (798, '231084', '宁安市', 3, '2310', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (799, '231085', '穆棱市', 3, '2310', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (800, '231086', '东宁市', 3, '2310', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (801, '2311', '黑河市', 2, '23', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (802, '231102', '爱辉区', 3, '2311', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (803, '231123', '逊克县', 3, '2311', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (804, '231124', '孙吴县', 3, '2311', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (805, '231181', '北安市', 3, '2311', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (806, '231182', '五大连池市', 3, '2311', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (807, '231183', '嫩江市', 3, '2311', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (808, '2312', '绥化市', 2, '23', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (809, '231202', '北林区', 3, '2312', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (810, '231221', '望奎县', 3, '2312', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (811, '231222', '兰西县', 3, '2312', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (812, '231223', '青冈县', 3, '2312', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (813, '231224', '庆安县', 3, '2312', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (814, '231225', '明水县', 3, '2312', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (815, '231226', '绥棱县', 3, '2312', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (816, '231281', '安达市', 3, '2312', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (817, '231282', '肇东市', 3, '2312', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (818, '231283', '海伦市', 3, '2312', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (819, '2327', '大兴安岭地区', 2, '23', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (820, '232701', '漠河市', 3, '2327', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (821, '232721', '呼玛县', 3, '2327', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (822, '232722', '塔河县', 3, '2327', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (823, '232761', '加格达奇区', 3, '2327', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (824, '232762', '松岭区', 3, '2327', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (825, '232763', '新林区', 3, '2327', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (826, '232764', '呼中区', 3, '2327', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (827, '31', '上海市', 1, NULL, 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (828, '3101', '市辖区', 2, '31', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (829, '310101', '黄浦区', 3, '3101', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (830, '310104', '徐汇区', 3, '3101', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (831, '310105', '长宁区', 3, '3101', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (832, '310106', '静安区', 3, '3101', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (833, '310107', '普陀区', 3, '3101', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (834, '310109', '虹口区', 3, '3101', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (835, '310110', '杨浦区', 3, '3101', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (836, '310112', '闵行区', 3, '3101', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (837, '310113', '宝山区', 3, '3101', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (838, '310114', '嘉定区', 3, '3101', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (839, '310115', '浦东新区', 3, '3101', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (840, '310116', '金山区', 3, '3101', 12, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (841, '310117', '松江区', 3, '3101', 13, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (842, '310118', '青浦区', 3, '3101', 14, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (843, '310120', '奉贤区', 3, '3101', 15, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (844, '310151', '崇明区', 3, '3101', 16, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (845, '32', '江苏省', 1, NULL, 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (846, '3201', '南京市', 2, '32', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (847, '320102', '玄武区', 3, '3201', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (848, '320104', '秦淮区', 3, '3201', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (849, '320105', '建邺区', 3, '3201', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (850, '320106', '鼓楼区', 3, '3201', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (851, '320111', '浦口区', 3, '3201', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (852, '320113', '栖霞区', 3, '3201', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (853, '320114', '雨花台区', 3, '3201', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (854, '320115', '江宁区', 3, '3201', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (855, '320116', '六合区', 3, '3201', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (856, '320117', '溧水区', 3, '3201', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (857, '320118', '高淳区', 3, '3201', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (858, '3202', '无锡市', 2, '32', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (859, '320205', '锡山区', 3, '3202', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (860, '320206', '惠山区', 3, '3202', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (861, '320211', '滨湖区', 3, '3202', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (862, '320213', '梁溪区', 3, '3202', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (863, '320214', '新吴区', 3, '3202', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (864, '320281', '江阴市', 3, '3202', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (865, '320282', '宜兴市', 3, '3202', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (866, '3203', '徐州市', 2, '32', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (867, '320302', '鼓楼区', 3, '3203', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (868, '320303', '云龙区', 3, '3203', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (869, '320305', '贾汪区', 3, '3203', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (870, '320311', '泉山区', 3, '3203', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (871, '320312', '铜山区', 3, '3203', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (872, '320321', '丰县', 3, '3203', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (873, '320322', '沛县', 3, '3203', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (874, '320324', '睢宁县', 3, '3203', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (875, '320371', '徐州经济技术开发区', 3, '3203', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (876, '320381', '新沂市', 3, '3203', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (877, '320382', '邳州市', 3, '3203', 11, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (878, '3204', '常州市', 2, '32', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (879, '320402', '天宁区', 3, '3204', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (880, '320404', '钟楼区', 3, '3204', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (881, '320411', '新北区', 3, '3204', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (882, '320412', '武进区', 3, '3204', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (883, '320413', '金坛区', 3, '3204', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (884, '320481', '溧阳市', 3, '3204', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (885, '3205', '苏州市', 2, '32', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (886, '320505', '虎丘区', 3, '3205', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (887, '320506', '吴中区', 3, '3205', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (888, '320507', '相城区', 3, '3205', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (889, '320508', '姑苏区', 3, '3205', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (890, '320509', '吴江区', 3, '3205', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (891, '320576', '苏州工业园区', 3, '3205', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (892, '320581', '常熟市', 3, '3205', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (893, '320582', '张家港市', 3, '3205', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (894, '320583', '昆山市', 3, '3205', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (895, '320585', '太仓市', 3, '3205', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (896, '3206', '南通市', 2, '32', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (897, '320612', '通州区', 3, '3206', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (898, '320613', '崇川区', 3, '3206', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (899, '320614', '海门区', 3, '3206', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (900, '320623', '如东县', 3, '3206', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (901, '320671', '南通经济技术开发区', 3, '3206', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (902, '320681', '启东市', 3, '3206', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (903, '320682', '如皋市', 3, '3206', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (904, '320685', '海安市', 3, '3206', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (905, '3207', '连云港市', 2, '32', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (906, '320703', '连云区', 3, '3207', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (907, '320706', '海州区', 3, '3207', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (908, '320707', '赣榆区', 3, '3207', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (909, '320722', '东海县', 3, '3207', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (910, '320723', '灌云县', 3, '3207', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (911, '320724', '灌南县', 3, '3207', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (912, '320771', '连云港经济技术开发区', 3, '3207', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (913, '3208', '淮安市', 2, '32', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (914, '320803', '淮安区', 3, '3208', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (915, '320804', '淮阴区', 3, '3208', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (916, '320812', '清江浦区', 3, '3208', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (917, '320813', '洪泽区', 3, '3208', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (918, '320826', '涟水县', 3, '3208', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (919, '320830', '盱眙县', 3, '3208', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (920, '320831', '金湖县', 3, '3208', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (921, '320871', '淮安经济技术开发区', 3, '3208', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (922, '3209', '盐城市', 2, '32', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (923, '320902', '亭湖区', 3, '3209', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (924, '320903', '盐都区', 3, '3209', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (925, '320904', '大丰区', 3, '3209', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (926, '320921', '响水县', 3, '3209', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (927, '320922', '滨海县', 3, '3209', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (928, '320923', '阜宁县', 3, '3209', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (929, '320924', '射阳县', 3, '3209', 7, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (930, '320925', '建湖县', 3, '3209', 8, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (931, '320971', '盐城经济技术开发区', 3, '3209', 9, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (932, '320981', '东台市', 3, '3209', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (933, '3210', '扬州市', 2, '32', 10, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (934, '321002', '广陵区', 3, '3210', 1, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (935, '321003', '邗江区', 3, '3210', 2, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (936, '321012', '江都区', 3, '3210', 3, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (937, '321023', '宝应县', 3, '3210', 4, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (938, '321071', '扬州经济技术开发区', 3, '3210', 5, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (939, '321081', '仪征市', 3, '3210', 6, 1, '2025-10-02 02:34:56', '2025-10-02 02:34:56');
INSERT INTO `china_region` VALUES (940, '321084', '高邮市', 3, '3210', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (941, '3211', '镇江市', 2, '32', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (942, '321102', '京口区', 3, '3211', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (943, '321111', '润州区', 3, '3211', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (944, '321112', '丹徒区', 3, '3211', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (945, '321171', '镇江新区', 3, '3211', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (946, '321181', '丹阳市', 3, '3211', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (947, '321182', '扬中市', 3, '3211', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (948, '321183', '句容市', 3, '3211', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (949, '3212', '泰州市', 2, '32', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (950, '321202', '海陵区', 3, '3212', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (951, '321203', '高港区', 3, '3212', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (952, '321204', '姜堰区', 3, '3212', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (953, '321281', '兴化市', 3, '3212', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (954, '321282', '靖江市', 3, '3212', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (955, '321283', '泰兴市', 3, '3212', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (956, '3213', '宿迁市', 2, '32', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (957, '321302', '宿城区', 3, '3213', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (958, '321311', '宿豫区', 3, '3213', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (959, '321322', '沭阳县', 3, '3213', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (960, '321323', '泗阳县', 3, '3213', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (961, '321324', '泗洪县', 3, '3213', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (962, '321371', '宿迁经济技术开发区', 3, '3213', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (963, '33', '浙江省', 1, NULL, 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (964, '3301', '杭州市', 2, '33', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (965, '330102', '上城区', 3, '3301', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (966, '330105', '拱墅区', 3, '3301', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (967, '330106', '西湖区', 3, '3301', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (968, '330108', '滨江区', 3, '3301', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (969, '330109', '萧山区', 3, '3301', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (970, '330110', '余杭区', 3, '3301', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (971, '330111', '富阳区', 3, '3301', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (972, '330112', '临安区', 3, '3301', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (973, '330113', '临平区', 3, '3301', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (974, '330114', '钱塘区', 3, '3301', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (975, '330122', '桐庐县', 3, '3301', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (976, '330127', '淳安县', 3, '3301', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (977, '330182', '建德市', 3, '3301', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (978, '3302', '宁波市', 2, '33', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (979, '330203', '海曙区', 3, '3302', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (980, '330205', '江北区', 3, '3302', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (981, '330206', '北仑区', 3, '3302', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (982, '330211', '镇海区', 3, '3302', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (983, '330212', '鄞州区', 3, '3302', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (984, '330213', '奉化区', 3, '3302', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (985, '330225', '象山县', 3, '3302', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (986, '330226', '宁海县', 3, '3302', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (987, '330281', '余姚市', 3, '3302', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (988, '330282', '慈溪市', 3, '3302', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (989, '3303', '温州市', 2, '33', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (990, '330302', '鹿城区', 3, '3303', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (991, '330303', '龙湾区', 3, '3303', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (992, '330304', '瓯海区', 3, '3303', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (993, '330305', '洞头区', 3, '3303', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (994, '330324', '永嘉县', 3, '3303', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (995, '330326', '平阳县', 3, '3303', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (996, '330327', '苍南县', 3, '3303', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (997, '330328', '文成县', 3, '3303', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (998, '330329', '泰顺县', 3, '3303', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (999, '330381', '瑞安市', 3, '3303', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1000, '330382', '乐清市', 3, '3303', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1001, '330383', '龙港市', 3, '3303', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1002, '3304', '嘉兴市', 2, '33', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1003, '330402', '南湖区', 3, '3304', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1004, '330411', '秀洲区', 3, '3304', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1005, '330421', '嘉善县', 3, '3304', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1006, '330424', '海盐县', 3, '3304', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1007, '330481', '海宁市', 3, '3304', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1008, '330482', '平湖市', 3, '3304', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1009, '330483', '桐乡市', 3, '3304', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1010, '3305', '湖州市', 2, '33', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1011, '330502', '吴兴区', 3, '3305', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1012, '330503', '南浔区', 3, '3305', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1013, '330521', '德清县', 3, '3305', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1014, '330522', '长兴县', 3, '3305', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1015, '330523', '安吉县', 3, '3305', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1016, '3306', '绍兴市', 2, '33', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1017, '330602', '越城区', 3, '3306', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1018, '330603', '柯桥区', 3, '3306', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1019, '330604', '上虞区', 3, '3306', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1020, '330624', '新昌县', 3, '3306', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1021, '330681', '诸暨市', 3, '3306', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1022, '330683', '嵊州市', 3, '3306', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1023, '3307', '金华市', 2, '33', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1024, '330702', '婺城区', 3, '3307', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1025, '330703', '金东区', 3, '3307', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1026, '330723', '武义县', 3, '3307', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1027, '330726', '浦江县', 3, '3307', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1028, '330727', '磐安县', 3, '3307', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1029, '330781', '兰溪市', 3, '3307', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1030, '330782', '义乌市', 3, '3307', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1031, '330783', '东阳市', 3, '3307', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1032, '330784', '永康市', 3, '3307', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1033, '3308', '衢州市', 2, '33', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1034, '330802', '柯城区', 3, '3308', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1035, '330803', '衢江区', 3, '3308', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1036, '330822', '常山县', 3, '3308', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1037, '330824', '开化县', 3, '3308', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1038, '330825', '龙游县', 3, '3308', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1039, '330881', '江山市', 3, '3308', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1040, '3309', '舟山市', 2, '33', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1041, '330902', '定海区', 3, '3309', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1042, '330903', '普陀区', 3, '3309', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1043, '330921', '岱山县', 3, '3309', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1044, '330922', '嵊泗县', 3, '3309', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1045, '3310', '台州市', 2, '33', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1046, '331002', '椒江区', 3, '3310', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1047, '331003', '黄岩区', 3, '3310', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1048, '331004', '路桥区', 3, '3310', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1049, '331022', '三门县', 3, '3310', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1050, '331023', '天台县', 3, '3310', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1051, '331024', '仙居县', 3, '3310', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1052, '331081', '温岭市', 3, '3310', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1053, '331082', '临海市', 3, '3310', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1054, '331083', '玉环市', 3, '3310', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1055, '3311', '丽水市', 2, '33', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1056, '331102', '莲都区', 3, '3311', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1057, '331121', '青田县', 3, '3311', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1058, '331122', '缙云县', 3, '3311', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1059, '331123', '遂昌县', 3, '3311', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1060, '331124', '松阳县', 3, '3311', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1061, '331125', '云和县', 3, '3311', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1062, '331126', '庆元县', 3, '3311', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1063, '331127', '景宁畲族自治县', 3, '3311', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1064, '331181', '龙泉市', 3, '3311', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1065, '34', '安徽省', 1, NULL, 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1066, '3401', '合肥市', 2, '34', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1067, '340102', '瑶海区', 3, '3401', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1068, '340103', '庐阳区', 3, '3401', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1069, '340104', '蜀山区', 3, '3401', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1070, '340111', '包河区', 3, '3401', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1071, '340121', '长丰县', 3, '3401', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1072, '340122', '肥东县', 3, '3401', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1073, '340123', '肥西县', 3, '3401', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1074, '340124', '庐江县', 3, '3401', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1075, '340176', '合肥高新技术产业开发区', 3, '3401', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1076, '340177', '合肥经济技术开发区', 3, '3401', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1077, '340178', '合肥新站高新技术产业开发区', 3, '3401', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1078, '340181', '巢湖市', 3, '3401', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1079, '3402', '芜湖市', 2, '34', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1080, '340202', '镜湖区', 3, '3402', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1081, '340207', '鸠江区', 3, '3402', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1082, '340209', '弋江区', 3, '3402', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1083, '340210', '湾沚区', 3, '3402', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1084, '340212', '繁昌区', 3, '3402', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1085, '340223', '南陵县', 3, '3402', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1086, '340271', '芜湖经济技术开发区', 3, '3402', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1087, '340272', '安徽芜湖三山经济开发区', 3, '3402', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1088, '340281', '无为市', 3, '3402', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1089, '3403', '蚌埠市', 2, '34', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1090, '340302', '龙子湖区', 3, '3403', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1091, '340303', '蚌山区', 3, '3403', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1092, '340304', '禹会区', 3, '3403', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1093, '340311', '淮上区', 3, '3403', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1094, '340321', '怀远县', 3, '3403', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1095, '340322', '五河县', 3, '3403', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1096, '340323', '固镇县', 3, '3403', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1097, '340371', '蚌埠市高新技术开发区', 3, '3403', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1098, '340372', '蚌埠市经济开发区', 3, '3403', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1099, '3404', '淮南市', 2, '34', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1100, '340402', '大通区', 3, '3404', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1101, '340403', '田家庵区', 3, '3404', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1102, '340404', '谢家集区', 3, '3404', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1103, '340405', '八公山区', 3, '3404', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1104, '340406', '潘集区', 3, '3404', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1105, '340421', '凤台县', 3, '3404', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1106, '340422', '寿县', 3, '3404', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1107, '3405', '马鞍山市', 2, '34', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1108, '340503', '花山区', 3, '3405', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1109, '340504', '雨山区', 3, '3405', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1110, '340506', '博望区', 3, '3405', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1111, '340521', '当涂县', 3, '3405', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1112, '340522', '含山县', 3, '3405', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1113, '340523', '和县', 3, '3405', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1114, '3406', '淮北市', 2, '34', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1115, '340602', '杜集区', 3, '3406', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1116, '340603', '相山区', 3, '3406', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1117, '340604', '烈山区', 3, '3406', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1118, '340621', '濉溪县', 3, '3406', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1119, '3407', '铜陵市', 2, '34', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1120, '340705', '铜官区', 3, '3407', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1121, '340706', '义安区', 3, '3407', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1122, '340711', '郊区', 3, '3407', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1123, '340722', '枞阳县', 3, '3407', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1124, '3408', '安庆市', 2, '34', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1125, '340802', '迎江区', 3, '3408', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1126, '340803', '大观区', 3, '3408', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1127, '340811', '宜秀区', 3, '3408', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1128, '340822', '怀宁县', 3, '3408', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1129, '340825', '太湖县', 3, '3408', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1130, '340826', '宿松县', 3, '3408', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1131, '340827', '望江县', 3, '3408', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1132, '340828', '岳西县', 3, '3408', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1133, '340871', '安徽安庆经济开发区', 3, '3408', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1134, '340881', '桐城市', 3, '3408', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1135, '340882', '潜山市', 3, '3408', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1136, '3410', '黄山市', 2, '34', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1137, '341002', '屯溪区', 3, '3410', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1138, '341003', '黄山区', 3, '3410', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1139, '341004', '徽州区', 3, '3410', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1140, '341021', '歙县', 3, '3410', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1141, '341022', '休宁县', 3, '3410', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1142, '341023', '黟县', 3, '3410', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1143, '341024', '祁门县', 3, '3410', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1144, '3411', '滁州市', 2, '34', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1145, '341102', '琅琊区', 3, '3411', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1146, '341103', '南谯区', 3, '3411', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1147, '341122', '来安县', 3, '3411', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1148, '341124', '全椒县', 3, '3411', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1149, '341125', '定远县', 3, '3411', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1150, '341126', '凤阳县', 3, '3411', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1151, '341171', '中新苏滁高新技术产业开发区', 3, '3411', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1152, '341172', '滁州经济技术开发区', 3, '3411', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1153, '341181', '天长市', 3, '3411', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1154, '341182', '明光市', 3, '3411', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1155, '3412', '阜阳市', 2, '34', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1156, '341202', '颍州区', 3, '3412', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1157, '341203', '颍东区', 3, '3412', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1158, '341204', '颍泉区', 3, '3412', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1159, '341221', '临泉县', 3, '3412', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1160, '341222', '太和县', 3, '3412', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1161, '341225', '阜南县', 3, '3412', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1162, '341226', '颍上县', 3, '3412', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1163, '341271', '阜阳合肥现代产业园区', 3, '3412', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1164, '341272', '阜阳经济技术开发区', 3, '3412', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1165, '341282', '界首市', 3, '3412', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1166, '3413', '宿州市', 2, '34', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1167, '341302', '埇桥区', 3, '3413', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1168, '341321', '砀山县', 3, '3413', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1169, '341322', '萧县', 3, '3413', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1170, '341323', '灵璧县', 3, '3413', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1171, '341324', '泗县', 3, '3413', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1172, '341371', '宿州马鞍山现代产业园区', 3, '3413', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1173, '341372', '宿州经济技术开发区', 3, '3413', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1174, '3415', '六安市', 2, '34', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1175, '341502', '金安区', 3, '3415', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1176, '341503', '裕安区', 3, '3415', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1177, '341504', '叶集区', 3, '3415', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1178, '341522', '霍邱县', 3, '3415', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1179, '341523', '舒城县', 3, '3415', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1180, '341524', '金寨县', 3, '3415', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1181, '341525', '霍山县', 3, '3415', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1182, '3416', '亳州市', 2, '34', 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1183, '341602', '谯城区', 3, '3416', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1184, '341621', '涡阳县', 3, '3416', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1185, '341622', '蒙城县', 3, '3416', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1186, '341623', '利辛县', 3, '3416', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1187, '3417', '池州市', 2, '34', 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1188, '341702', '贵池区', 3, '3417', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1189, '341721', '东至县', 3, '3417', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1190, '341722', '石台县', 3, '3417', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1191, '341723', '青阳县', 3, '3417', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1192, '3418', '宣城市', 2, '34', 16, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1193, '341802', '宣州区', 3, '3418', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1194, '341821', '郎溪县', 3, '3418', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1195, '341823', '泾县', 3, '3418', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1196, '341824', '绩溪县', 3, '3418', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1197, '341825', '旌德县', 3, '3418', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1198, '341871', '宣城市经济开发区', 3, '3418', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1199, '341881', '宁国市', 3, '3418', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1200, '341882', '广德市', 3, '3418', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1201, '35', '福建省', 1, NULL, 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1202, '3501', '福州市', 2, '35', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1203, '350102', '鼓楼区', 3, '3501', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1204, '350103', '台江区', 3, '3501', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1205, '350104', '仓山区', 3, '3501', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1206, '350105', '马尾区', 3, '3501', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1207, '350111', '晋安区', 3, '3501', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1208, '350112', '长乐区', 3, '3501', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1209, '350121', '闽侯县', 3, '3501', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1210, '350122', '连江县', 3, '3501', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1211, '350123', '罗源县', 3, '3501', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1212, '350124', '闽清县', 3, '3501', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1213, '350125', '永泰县', 3, '3501', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1214, '350128', '平潭县', 3, '3501', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1215, '350181', '福清市', 3, '3501', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1216, '3502', '厦门市', 2, '35', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1217, '350203', '思明区', 3, '3502', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1218, '350205', '海沧区', 3, '3502', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1219, '350206', '湖里区', 3, '3502', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1220, '350211', '集美区', 3, '3502', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1221, '350212', '同安区', 3, '3502', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1222, '350213', '翔安区', 3, '3502', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1223, '3503', '莆田市', 2, '35', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1224, '350302', '城厢区', 3, '3503', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1225, '350303', '涵江区', 3, '3503', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1226, '350304', '荔城区', 3, '3503', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1227, '350305', '秀屿区', 3, '3503', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1228, '350322', '仙游县', 3, '3503', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1229, '3504', '三明市', 2, '35', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1230, '350404', '三元区', 3, '3504', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1231, '350405', '沙县区', 3, '3504', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1232, '350421', '明溪县', 3, '3504', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1233, '350423', '清流县', 3, '3504', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1234, '350424', '宁化县', 3, '3504', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1235, '350425', '大田县', 3, '3504', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1236, '350426', '尤溪县', 3, '3504', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1237, '350428', '将乐县', 3, '3504', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1238, '350429', '泰宁县', 3, '3504', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1239, '350430', '建宁县', 3, '3504', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1240, '350481', '永安市', 3, '3504', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1241, '3505', '泉州市', 2, '35', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1242, '350502', '鲤城区', 3, '3505', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1243, '350503', '丰泽区', 3, '3505', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1244, '350504', '洛江区', 3, '3505', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1245, '350505', '泉港区', 3, '3505', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1246, '350521', '惠安县', 3, '3505', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1247, '350524', '安溪县', 3, '3505', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1248, '350525', '永春县', 3, '3505', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1249, '350526', '德化县', 3, '3505', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1250, '350527', '金门县', 3, '3505', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1251, '350581', '石狮市', 3, '3505', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1252, '350582', '晋江市', 3, '3505', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1253, '350583', '南安市', 3, '3505', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1254, '3506', '漳州市', 2, '35', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1255, '350602', '芗城区', 3, '3506', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1256, '350603', '龙文区', 3, '3506', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1257, '350604', '龙海区', 3, '3506', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1258, '350605', '长泰区', 3, '3506', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1259, '350622', '云霄县', 3, '3506', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1260, '350623', '漳浦县', 3, '3506', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1261, '350624', '诏安县', 3, '3506', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1262, '350626', '东山县', 3, '3506', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1263, '350627', '南靖县', 3, '3506', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1264, '350628', '平和县', 3, '3506', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1265, '350629', '华安县', 3, '3506', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1266, '3507', '南平市', 2, '35', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1267, '350702', '延平区', 3, '3507', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1268, '350703', '建阳区', 3, '3507', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1269, '350721', '顺昌县', 3, '3507', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1270, '350722', '浦城县', 3, '3507', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1271, '350723', '光泽县', 3, '3507', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1272, '350724', '松溪县', 3, '3507', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1273, '350725', '政和县', 3, '3507', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1274, '350781', '邵武市', 3, '3507', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1275, '350782', '武夷山市', 3, '3507', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1276, '350783', '建瓯市', 3, '3507', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1277, '3508', '龙岩市', 2, '35', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1278, '350802', '新罗区', 3, '3508', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1279, '350803', '永定区', 3, '3508', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1280, '350821', '长汀县', 3, '3508', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1281, '350823', '上杭县', 3, '3508', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1282, '350824', '武平县', 3, '3508', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1283, '350825', '连城县', 3, '3508', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1284, '350881', '漳平市', 3, '3508', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1285, '3509', '宁德市', 2, '35', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1286, '350902', '蕉城区', 3, '3509', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1287, '350921', '霞浦县', 3, '3509', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1288, '350922', '古田县', 3, '3509', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1289, '350923', '屏南县', 3, '3509', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1290, '350924', '寿宁县', 3, '3509', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1291, '350925', '周宁县', 3, '3509', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1292, '350926', '柘荣县', 3, '3509', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1293, '350981', '福安市', 3, '3509', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1294, '350982', '福鼎市', 3, '3509', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1295, '36', '江西省', 1, NULL, 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1296, '3601', '南昌市', 2, '36', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1297, '360102', '东湖区', 3, '3601', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1298, '360103', '西湖区', 3, '3601', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1299, '360104', '青云谱区', 3, '3601', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1300, '360111', '青山湖区', 3, '3601', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1301, '360112', '新建区', 3, '3601', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1302, '360113', '红谷滩区', 3, '3601', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1303, '360121', '南昌县', 3, '3601', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1304, '360123', '安义县', 3, '3601', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1305, '360124', '进贤县', 3, '3601', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1306, '3602', '景德镇市', 2, '36', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1307, '360202', '昌江区', 3, '3602', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1308, '360203', '珠山区', 3, '3602', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1309, '360222', '浮梁县', 3, '3602', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1310, '360281', '乐平市', 3, '3602', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1311, '3603', '萍乡市', 2, '36', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1312, '360302', '安源区', 3, '3603', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1313, '360313', '湘东区', 3, '3603', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1314, '360321', '莲花县', 3, '3603', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1315, '360322', '上栗县', 3, '3603', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1316, '360323', '芦溪县', 3, '3603', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1317, '3604', '九江市', 2, '36', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1318, '360402', '濂溪区', 3, '3604', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1319, '360403', '浔阳区', 3, '3604', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1320, '360404', '柴桑区', 3, '3604', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1321, '360423', '武宁县', 3, '3604', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1322, '360424', '修水县', 3, '3604', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1323, '360425', '永修县', 3, '3604', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1324, '360426', '德安县', 3, '3604', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1325, '360428', '都昌县', 3, '3604', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1326, '360429', '湖口县', 3, '3604', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1327, '360430', '彭泽县', 3, '3604', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1328, '360481', '瑞昌市', 3, '3604', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1329, '360482', '共青城市', 3, '3604', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1330, '360483', '庐山市', 3, '3604', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1331, '3605', '新余市', 2, '36', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1332, '360502', '渝水区', 3, '3605', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1333, '360521', '分宜县', 3, '3605', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1334, '3606', '鹰潭市', 2, '36', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1335, '360602', '月湖区', 3, '3606', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1336, '360603', '余江区', 3, '3606', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1337, '360681', '贵溪市', 3, '3606', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1338, '3607', '赣州市', 2, '36', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1339, '360702', '章贡区', 3, '3607', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1340, '360703', '南康区', 3, '3607', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1341, '360704', '赣县区', 3, '3607', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1342, '360722', '信丰县', 3, '3607', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1343, '360723', '大余县', 3, '3607', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1344, '360724', '上犹县', 3, '3607', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1345, '360725', '崇义县', 3, '3607', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1346, '360726', '安远县', 3, '3607', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1347, '360728', '定南县', 3, '3607', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1348, '360729', '全南县', 3, '3607', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1349, '360730', '宁都县', 3, '3607', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1350, '360731', '于都县', 3, '3607', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1351, '360732', '兴国县', 3, '3607', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1352, '360733', '会昌县', 3, '3607', 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1353, '360734', '寻乌县', 3, '3607', 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1354, '360735', '石城县', 3, '3607', 16, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1355, '360781', '瑞金市', 3, '3607', 17, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1356, '360783', '龙南市', 3, '3607', 18, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1357, '3608', '吉安市', 2, '36', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1358, '360802', '吉州区', 3, '3608', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1359, '360803', '青原区', 3, '3608', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1360, '360821', '吉安县', 3, '3608', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1361, '360822', '吉水县', 3, '3608', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1362, '360823', '峡江县', 3, '3608', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1363, '360824', '新干县', 3, '3608', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1364, '360825', '永丰县', 3, '3608', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1365, '360826', '泰和县', 3, '3608', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1366, '360827', '遂川县', 3, '3608', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1367, '360828', '万安县', 3, '3608', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1368, '360829', '安福县', 3, '3608', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1369, '360830', '永新县', 3, '3608', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1370, '360881', '井冈山市', 3, '3608', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1371, '3609', '宜春市', 2, '36', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1372, '360902', '袁州区', 3, '3609', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1373, '360921', '奉新县', 3, '3609', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1374, '360922', '万载县', 3, '3609', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1375, '360923', '上高县', 3, '3609', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1376, '360924', '宜丰县', 3, '3609', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1377, '360925', '靖安县', 3, '3609', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1378, '360926', '铜鼓县', 3, '3609', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1379, '360981', '丰城市', 3, '3609', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1380, '360982', '樟树市', 3, '3609', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1381, '360983', '高安市', 3, '3609', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1382, '3610', '抚州市', 2, '36', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1383, '361002', '临川区', 3, '3610', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1384, '361003', '东乡区', 3, '3610', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1385, '361021', '南城县', 3, '3610', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1386, '361022', '黎川县', 3, '3610', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1387, '361023', '南丰县', 3, '3610', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1388, '361024', '崇仁县', 3, '3610', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1389, '361025', '乐安县', 3, '3610', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1390, '361026', '宜黄县', 3, '3610', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1391, '361027', '金溪县', 3, '3610', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1392, '361028', '资溪县', 3, '3610', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1393, '361030', '广昌县', 3, '3610', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1394, '3611', '上饶市', 2, '36', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1395, '361102', '信州区', 3, '3611', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1396, '361103', '广丰区', 3, '3611', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1397, '361104', '广信区', 3, '3611', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1398, '361123', '玉山县', 3, '3611', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1399, '361124', '铅山县', 3, '3611', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1400, '361125', '横峰县', 3, '3611', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1401, '361126', '弋阳县', 3, '3611', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1402, '361127', '余干县', 3, '3611', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1403, '361128', '鄱阳县', 3, '3611', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1404, '361129', '万年县', 3, '3611', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1405, '361130', '婺源县', 3, '3611', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1406, '361181', '德兴市', 3, '3611', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1407, '37', '山东省', 1, NULL, 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1408, '3701', '济南市', 2, '37', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1409, '370102', '历下区', 3, '3701', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1410, '370103', '市中区', 3, '3701', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1411, '370104', '槐荫区', 3, '3701', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1412, '370105', '天桥区', 3, '3701', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1413, '370112', '历城区', 3, '3701', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1414, '370113', '长清区', 3, '3701', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1415, '370114', '章丘区', 3, '3701', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1416, '370115', '济阳区', 3, '3701', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1417, '370116', '莱芜区', 3, '3701', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1418, '370117', '钢城区', 3, '3701', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1419, '370124', '平阴县', 3, '3701', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1420, '370126', '商河县', 3, '3701', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1421, '370176', '济南高新技术产业开发区', 3, '3701', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1422, '3702', '青岛市', 2, '37', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1423, '370202', '市南区', 3, '3702', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1424, '370203', '市北区', 3, '3702', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1425, '370211', '黄岛区', 3, '3702', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1426, '370212', '崂山区', 3, '3702', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1427, '370213', '李沧区', 3, '3702', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1428, '370214', '城阳区', 3, '3702', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1429, '370215', '即墨区', 3, '3702', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1430, '370281', '胶州市', 3, '3702', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1431, '370283', '平度市', 3, '3702', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1432, '370285', '莱西市', 3, '3702', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1433, '3703', '淄博市', 2, '37', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1434, '370302', '淄川区', 3, '3703', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1435, '370303', '张店区', 3, '3703', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1436, '370304', '博山区', 3, '3703', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1437, '370305', '临淄区', 3, '3703', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1438, '370306', '周村区', 3, '3703', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1439, '370321', '桓台县', 3, '3703', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1440, '370322', '高青县', 3, '3703', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1441, '370323', '沂源县', 3, '3703', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1442, '3704', '枣庄市', 2, '37', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1443, '370402', '市中区', 3, '3704', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1444, '370403', '薛城区', 3, '3704', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1445, '370404', '峄城区', 3, '3704', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1446, '370405', '台儿庄区', 3, '3704', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1447, '370406', '山亭区', 3, '3704', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1448, '370481', '滕州市', 3, '3704', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1449, '3705', '东营市', 2, '37', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1450, '370502', '东营区', 3, '3705', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1451, '370503', '河口区', 3, '3705', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1452, '370505', '垦利区', 3, '3705', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1453, '370522', '利津县', 3, '3705', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1454, '370523', '广饶县', 3, '3705', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1455, '370571', '东营经济技术开发区', 3, '3705', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1456, '370572', '东营港经济开发区', 3, '3705', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1457, '3706', '烟台市', 2, '37', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1458, '370602', '芝罘区', 3, '3706', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1459, '370611', '福山区', 3, '3706', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1460, '370612', '牟平区', 3, '3706', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1461, '370613', '莱山区', 3, '3706', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1462, '370614', '蓬莱区', 3, '3706', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1463, '370671', '烟台高新技术产业开发区', 3, '3706', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1464, '370676', '烟台经济技术开发区', 3, '3706', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1465, '370681', '龙口市', 3, '3706', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1466, '370682', '莱阳市', 3, '3706', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1467, '370683', '莱州市', 3, '3706', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1468, '370685', '招远市', 3, '3706', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1469, '370686', '栖霞市', 3, '3706', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1470, '370687', '海阳市', 3, '3706', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1471, '3707', '潍坊市', 2, '37', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1472, '370702', '潍城区', 3, '3707', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1473, '370703', '寒亭区', 3, '3707', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1474, '370704', '坊子区', 3, '3707', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1475, '370705', '奎文区', 3, '3707', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1476, '370724', '临朐县', 3, '3707', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1477, '370725', '昌乐县', 3, '3707', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1478, '370772', '潍坊滨海经济技术开发区', 3, '3707', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1479, '370781', '青州市', 3, '3707', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1480, '370782', '诸城市', 3, '3707', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1481, '370783', '寿光市', 3, '3707', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1482, '370784', '安丘市', 3, '3707', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1483, '370785', '高密市', 3, '3707', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1484, '370786', '昌邑市', 3, '3707', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1485, '3708', '济宁市', 2, '37', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1486, '370811', '任城区', 3, '3708', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1487, '370812', '兖州区', 3, '3708', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1488, '370826', '微山县', 3, '3708', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1489, '370827', '鱼台县', 3, '3708', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1490, '370828', '金乡县', 3, '3708', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1491, '370829', '嘉祥县', 3, '3708', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1492, '370830', '汶上县', 3, '3708', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1493, '370831', '泗水县', 3, '3708', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1494, '370832', '梁山县', 3, '3708', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1495, '370871', '济宁高新技术产业开发区', 3, '3708', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1496, '370881', '曲阜市', 3, '3708', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1497, '370883', '邹城市', 3, '3708', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1498, '3709', '泰安市', 2, '37', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1499, '370902', '泰山区', 3, '3709', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1500, '370911', '岱岳区', 3, '3709', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1501, '370921', '宁阳县', 3, '3709', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1502, '370923', '东平县', 3, '3709', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1503, '370982', '新泰市', 3, '3709', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1504, '370983', '肥城市', 3, '3709', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1505, '3710', '威海市', 2, '37', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1506, '371002', '环翠区', 3, '3710', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1507, '371003', '文登区', 3, '3710', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1508, '371071', '威海火炬高技术产业开发区', 3, '3710', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1509, '371072', '威海经济技术开发区', 3, '3710', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1510, '371073', '威海临港经济技术开发区', 3, '3710', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1511, '371082', '荣成市', 3, '3710', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1512, '371083', '乳山市', 3, '3710', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1513, '3711', '日照市', 2, '37', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1514, '371102', '东港区', 3, '3711', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1515, '371103', '岚山区', 3, '3711', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1516, '371121', '五莲县', 3, '3711', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1517, '371122', '莒县', 3, '3711', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1518, '371171', '日照经济技术开发区', 3, '3711', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1519, '3713', '临沂市', 2, '37', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1520, '371302', '兰山区', 3, '3713', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1521, '371311', '罗庄区', 3, '3713', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1522, '371312', '河东区', 3, '3713', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1523, '371321', '沂南县', 3, '3713', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1524, '371322', '郯城县', 3, '3713', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1525, '371323', '沂水县', 3, '3713', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1526, '371324', '兰陵县', 3, '3713', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1527, '371325', '费县', 3, '3713', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1528, '371326', '平邑县', 3, '3713', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1529, '371327', '莒南县', 3, '3713', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1530, '371328', '蒙阴县', 3, '3713', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1531, '371329', '临沭县', 3, '3713', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1532, '371371', '临沂高新技术产业开发区', 3, '3713', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1533, '3714', '德州市', 2, '37', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1534, '371402', '德城区', 3, '3714', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1535, '371403', '陵城区', 3, '3714', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1536, '371422', '宁津县', 3, '3714', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1537, '371423', '庆云县', 3, '3714', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1538, '371424', '临邑县', 3, '3714', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1539, '371425', '齐河县', 3, '3714', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1540, '371426', '平原县', 3, '3714', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1541, '371427', '夏津县', 3, '3714', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1542, '371428', '武城县', 3, '3714', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1543, '371471', '德州天衢新区', 3, '3714', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1544, '371481', '乐陵市', 3, '3714', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1545, '371482', '禹城市', 3, '3714', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1546, '3715', '聊城市', 2, '37', 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1547, '371502', '东昌府区', 3, '3715', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1548, '371503', '茌平区', 3, '3715', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1549, '371521', '阳谷县', 3, '3715', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1550, '371522', '莘县', 3, '3715', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1551, '371524', '东阿县', 3, '3715', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1552, '371525', '冠县', 3, '3715', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1553, '371526', '高唐县', 3, '3715', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1554, '371581', '临清市', 3, '3715', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1555, '3716', '滨州市', 2, '37', 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1556, '371602', '滨城区', 3, '3716', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1557, '371603', '沾化区', 3, '3716', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1558, '371621', '惠民县', 3, '3716', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1559, '371622', '阳信县', 3, '3716', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1560, '371623', '无棣县', 3, '3716', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1561, '371625', '博兴县', 3, '3716', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1562, '371681', '邹平市', 3, '3716', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1563, '3717', '菏泽市', 2, '37', 16, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1564, '371702', '牡丹区', 3, '3717', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1565, '371703', '定陶区', 3, '3717', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1566, '371721', '曹县', 3, '3717', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1567, '371722', '单县', 3, '3717', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1568, '371723', '成武县', 3, '3717', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1569, '371724', '巨野县', 3, '3717', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1570, '371725', '郓城县', 3, '3717', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1571, '371726', '鄄城县', 3, '3717', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1572, '371728', '东明县', 3, '3717', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1573, '371771', '菏泽经济技术开发区', 3, '3717', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1574, '371772', '菏泽高新技术开发区', 3, '3717', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1575, '41', '河南省', 1, NULL, 16, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1576, '4101', '郑州市', 2, '41', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1577, '410102', '中原区', 3, '4101', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1578, '410103', '二七区', 3, '4101', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1579, '410104', '管城回族区', 3, '4101', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1580, '410105', '金水区', 3, '4101', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1581, '410106', '上街区', 3, '4101', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1582, '410108', '惠济区', 3, '4101', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1583, '410122', '中牟县', 3, '4101', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1584, '410171', '郑州经济技术开发区', 3, '4101', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1585, '410172', '郑州高新技术产业开发区', 3, '4101', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1586, '410173', '郑州航空港经济综合实验区', 3, '4101', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1587, '410181', '巩义市', 3, '4101', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1588, '410182', '荥阳市', 3, '4101', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1589, '410183', '新密市', 3, '4101', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1590, '410184', '新郑市', 3, '4101', 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1591, '410185', '登封市', 3, '4101', 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1592, '4102', '开封市', 2, '41', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1593, '410202', '龙亭区', 3, '4102', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1594, '410203', '顺河回族区', 3, '4102', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1595, '410204', '鼓楼区', 3, '4102', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1596, '410205', '禹王台区', 3, '4102', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1597, '410212', '祥符区', 3, '4102', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1598, '410221', '杞县', 3, '4102', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1599, '410222', '通许县', 3, '4102', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1600, '410223', '尉氏县', 3, '4102', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1601, '410225', '兰考县', 3, '4102', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1602, '4103', '洛阳市', 2, '41', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1603, '410302', '老城区', 3, '4103', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1604, '410303', '西工区', 3, '4103', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1605, '410304', '瀍河回族区', 3, '4103', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1606, '410305', '涧西区', 3, '4103', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1607, '410307', '偃师区', 3, '4103', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1608, '410308', '孟津区', 3, '4103', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1609, '410311', '洛龙区', 3, '4103', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1610, '410323', '新安县', 3, '4103', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1611, '410324', '栾川县', 3, '4103', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1612, '410325', '嵩县', 3, '4103', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1613, '410326', '汝阳县', 3, '4103', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1614, '410327', '宜阳县', 3, '4103', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1615, '410328', '洛宁县', 3, '4103', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1616, '410329', '伊川县', 3, '4103', 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1617, '410371', '洛阳高新技术产业开发区', 3, '4103', 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1618, '4104', '平顶山市', 2, '41', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1619, '410402', '新华区', 3, '4104', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1620, '410403', '卫东区', 3, '4104', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1621, '410404', '石龙区', 3, '4104', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1622, '410411', '湛河区', 3, '4104', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1623, '410421', '宝丰县', 3, '4104', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1624, '410422', '叶县', 3, '4104', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1625, '410423', '鲁山县', 3, '4104', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1626, '410425', '郏县', 3, '4104', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1627, '410471', '平顶山高新技术产业开发区', 3, '4104', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1628, '410472', '平顶山市城乡一体化示范区', 3, '4104', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1629, '410481', '舞钢市', 3, '4104', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1630, '410482', '汝州市', 3, '4104', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1631, '4105', '安阳市', 2, '41', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1632, '410502', '文峰区', 3, '4105', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1633, '410503', '北关区', 3, '4105', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1634, '410505', '殷都区', 3, '4105', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1635, '410506', '龙安区', 3, '4105', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1636, '410522', '安阳县', 3, '4105', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1637, '410523', '汤阴县', 3, '4105', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1638, '410526', '滑县', 3, '4105', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1639, '410527', '内黄县', 3, '4105', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1640, '410571', '安阳高新技术产业开发区', 3, '4105', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1641, '410581', '林州市', 3, '4105', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1642, '4106', '鹤壁市', 2, '41', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1643, '410602', '鹤山区', 3, '4106', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1644, '410603', '山城区', 3, '4106', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1645, '410611', '淇滨区', 3, '4106', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1646, '410621', '浚县', 3, '4106', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1647, '410622', '淇县', 3, '4106', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1648, '410671', '鹤壁经济技术开发区', 3, '4106', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1649, '4107', '新乡市', 2, '41', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1650, '410702', '红旗区', 3, '4107', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1651, '410703', '卫滨区', 3, '4107', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1652, '410704', '凤泉区', 3, '4107', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1653, '410711', '牧野区', 3, '4107', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1654, '410721', '新乡县', 3, '4107', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1655, '410724', '获嘉县', 3, '4107', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1656, '410725', '原阳县', 3, '4107', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1657, '410726', '延津县', 3, '4107', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1658, '410727', '封丘县', 3, '4107', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1659, '410771', '新乡高新技术产业开发区', 3, '4107', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1660, '410772', '新乡经济技术开发区', 3, '4107', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1661, '410773', '新乡市平原城乡一体化示范区', 3, '4107', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1662, '410781', '卫辉市', 3, '4107', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1663, '410782', '辉县市', 3, '4107', 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1664, '410783', '长垣市', 3, '4107', 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1665, '4108', '焦作市', 2, '41', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1666, '410802', '解放区', 3, '4108', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1667, '410803', '中站区', 3, '4108', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1668, '410804', '马村区', 3, '4108', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1669, '410811', '山阳区', 3, '4108', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1670, '410821', '修武县', 3, '4108', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1671, '410822', '博爱县', 3, '4108', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1672, '410823', '武陟县', 3, '4108', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1673, '410825', '温县', 3, '4108', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1674, '410871', '焦作城乡一体化示范区', 3, '4108', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1675, '410882', '沁阳市', 3, '4108', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1676, '410883', '孟州市', 3, '4108', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1677, '4109', '濮阳市', 2, '41', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1678, '410902', '华龙区', 3, '4109', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1679, '410922', '清丰县', 3, '4109', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1680, '410923', '南乐县', 3, '4109', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1681, '410926', '范县', 3, '4109', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1682, '410927', '台前县', 3, '4109', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1683, '410928', '濮阳县', 3, '4109', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1684, '410971', '河南濮阳工业园区', 3, '4109', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1685, '410972', '濮阳经济技术开发区', 3, '4109', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1686, '4110', '许昌市', 2, '41', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1687, '411002', '魏都区', 3, '4110', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1688, '411003', '建安区', 3, '4110', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1689, '411024', '鄢陵县', 3, '4110', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1690, '411025', '襄城县', 3, '4110', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1691, '411071', '许昌经济技术开发区', 3, '4110', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1692, '411081', '禹州市', 3, '4110', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1693, '411082', '长葛市', 3, '4110', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1694, '4111', '漯河市', 2, '41', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1695, '411102', '源汇区', 3, '4111', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1696, '411103', '郾城区', 3, '4111', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1697, '411104', '召陵区', 3, '4111', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1698, '411121', '舞阳县', 3, '4111', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1699, '411122', '临颍县', 3, '4111', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1700, '411171', '漯河经济技术开发区', 3, '4111', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1701, '4112', '三门峡市', 2, '41', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1702, '411202', '湖滨区', 3, '4112', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1703, '411203', '陕州区', 3, '4112', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1704, '411221', '渑池县', 3, '4112', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1705, '411224', '卢氏县', 3, '4112', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1706, '411271', '河南三门峡经济开发区', 3, '4112', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1707, '411281', '义马市', 3, '4112', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1708, '411282', '灵宝市', 3, '4112', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1709, '4113', '南阳市', 2, '41', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1710, '411302', '宛城区', 3, '4113', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1711, '411303', '卧龙区', 3, '4113', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1712, '411321', '南召县', 3, '4113', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1713, '411322', '方城县', 3, '4113', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1714, '411323', '西峡县', 3, '4113', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1715, '411324', '镇平县', 3, '4113', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1716, '411325', '内乡县', 3, '4113', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1717, '411326', '淅川县', 3, '4113', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1718, '411327', '社旗县', 3, '4113', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1719, '411328', '唐河县', 3, '4113', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1720, '411329', '新野县', 3, '4113', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1721, '411330', '桐柏县', 3, '4113', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1722, '411371', '南阳高新技术产业开发区', 3, '4113', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1723, '411372', '南阳市城乡一体化示范区', 3, '4113', 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1724, '411381', '邓州市', 3, '4113', 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1725, '4114', '商丘市', 2, '41', 14, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1726, '411402', '梁园区', 3, '4114', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1727, '411403', '睢阳区', 3, '4114', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1728, '411421', '民权县', 3, '4114', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1729, '411422', '睢县', 3, '4114', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1730, '411423', '宁陵县', 3, '4114', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1731, '411424', '柘城县', 3, '4114', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1732, '411425', '虞城县', 3, '4114', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1733, '411426', '夏邑县', 3, '4114', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1734, '411471', '豫东综合物流产业聚集区', 3, '4114', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1735, '411472', '河南商丘经济开发区', 3, '4114', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1736, '411481', '永城市', 3, '4114', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1737, '4115', '信阳市', 2, '41', 15, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1738, '411502', '浉河区', 3, '4115', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1739, '411503', '平桥区', 3, '4115', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1740, '411521', '罗山县', 3, '4115', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1741, '411522', '光山县', 3, '4115', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1742, '411523', '新县', 3, '4115', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1743, '411524', '商城县', 3, '4115', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1744, '411525', '固始县', 3, '4115', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1745, '411526', '潢川县', 3, '4115', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1746, '411527', '淮滨县', 3, '4115', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1747, '411528', '息县', 3, '4115', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1748, '411571', '信阳高新技术产业开发区', 3, '4115', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1749, '4116', '周口市', 2, '41', 16, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1750, '411602', '川汇区', 3, '4116', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1751, '411603', '淮阳区', 3, '4116', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1752, '411621', '扶沟县', 3, '4116', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1753, '411622', '西华县', 3, '4116', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1754, '411623', '商水县', 3, '4116', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1755, '411624', '沈丘县', 3, '4116', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1756, '411625', '郸城县', 3, '4116', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1757, '411627', '太康县', 3, '4116', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1758, '411628', '鹿邑县', 3, '4116', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1759, '411671', '周口临港开发区', 3, '4116', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1760, '411681', '项城市', 3, '4116', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1761, '4117', '驻马店市', 2, '41', 17, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1762, '411702', '驿城区', 3, '4117', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1763, '411721', '西平县', 3, '4117', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1764, '411722', '上蔡县', 3, '4117', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1765, '411723', '平舆县', 3, '4117', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1766, '411724', '正阳县', 3, '4117', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1767, '411725', '确山县', 3, '4117', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1768, '411726', '泌阳县', 3, '4117', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1769, '411727', '汝南县', 3, '4117', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1770, '411728', '遂平县', 3, '4117', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1771, '411729', '新蔡县', 3, '4117', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1772, '411771', '河南驻马店经济开发区', 3, '4117', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1773, '4190', '省直辖县级行政区划', 2, '41', 18, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1774, '419001', '济源市', 3, '4190', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1775, '42', '湖北省', 1, NULL, 17, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1776, '4201', '武汉市', 2, '42', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1777, '420102', '江岸区', 3, '4201', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1778, '420103', '江汉区', 3, '4201', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1779, '420104', '硚口区', 3, '4201', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1780, '420105', '汉阳区', 3, '4201', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1781, '420106', '武昌区', 3, '4201', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1782, '420107', '青山区', 3, '4201', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1783, '420111', '洪山区', 3, '4201', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1784, '420112', '东西湖区', 3, '4201', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1785, '420113', '汉南区', 3, '4201', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1786, '420114', '蔡甸区', 3, '4201', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1787, '420115', '江夏区', 3, '4201', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1788, '420116', '黄陂区', 3, '4201', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1789, '420117', '新洲区', 3, '4201', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1790, '4202', '黄石市', 2, '42', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1791, '420202', '黄石港区', 3, '4202', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1792, '420203', '西塞山区', 3, '4202', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1793, '420204', '下陆区', 3, '4202', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1794, '420205', '铁山区', 3, '4202', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1795, '420222', '阳新县', 3, '4202', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1796, '420281', '大冶市', 3, '4202', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1797, '4203', '十堰市', 2, '42', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1798, '420302', '茅箭区', 3, '4203', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1799, '420303', '张湾区', 3, '4203', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1800, '420304', '郧阳区', 3, '4203', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1801, '420322', '郧西县', 3, '4203', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1802, '420323', '竹山县', 3, '4203', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1803, '420324', '竹溪县', 3, '4203', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1804, '420325', '房县', 3, '4203', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1805, '420381', '丹江口市', 3, '4203', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1806, '4205', '宜昌市', 2, '42', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1807, '420502', '西陵区', 3, '4205', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1808, '420503', '伍家岗区', 3, '4205', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1809, '420504', '点军区', 3, '4205', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1810, '420505', '猇亭区', 3, '4205', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1811, '420506', '夷陵区', 3, '4205', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1812, '420525', '远安县', 3, '4205', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1813, '420526', '兴山县', 3, '4205', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1814, '420527', '秭归县', 3, '4205', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1815, '420528', '长阳土家族自治县', 3, '4205', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1816, '420529', '五峰土家族自治县', 3, '4205', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1817, '420581', '宜都市', 3, '4205', 11, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1818, '420582', '当阳市', 3, '4205', 12, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1819, '420583', '枝江市', 3, '4205', 13, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1820, '4206', '襄阳市', 2, '42', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1821, '420602', '襄城区', 3, '4206', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1822, '420606', '樊城区', 3, '4206', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1823, '420607', '襄州区', 3, '4206', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1824, '420624', '南漳县', 3, '4206', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1825, '420625', '谷城县', 3, '4206', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1826, '420626', '保康县', 3, '4206', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1827, '420682', '老河口市', 3, '4206', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1828, '420683', '枣阳市', 3, '4206', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1829, '420684', '宜城市', 3, '4206', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1830, '4207', '鄂州市', 2, '42', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1831, '420702', '梁子湖区', 3, '4207', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1832, '420703', '华容区', 3, '4207', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1833, '420704', '鄂城区', 3, '4207', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1834, '4208', '荆门市', 2, '42', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1835, '420802', '东宝区', 3, '4208', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1836, '420804', '掇刀区', 3, '4208', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1837, '420822', '沙洋县', 3, '4208', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1838, '420881', '钟祥市', 3, '4208', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1839, '420882', '京山市', 3, '4208', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1840, '4209', '孝感市', 2, '42', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1841, '420902', '孝南区', 3, '4209', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1842, '420921', '孝昌县', 3, '4209', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1843, '420922', '大悟县', 3, '4209', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1844, '420923', '云梦县', 3, '4209', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1845, '420981', '应城市', 3, '4209', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1846, '420982', '安陆市', 3, '4209', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1847, '420984', '汉川市', 3, '4209', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1848, '4210', '荆州市', 2, '42', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1849, '421002', '沙市区', 3, '4210', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1850, '421003', '荆州区', 3, '4210', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1851, '421022', '公安县', 3, '4210', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1852, '421024', '江陵县', 3, '4210', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1853, '421071', '荆州经济技术开发区', 3, '4210', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1854, '421081', '石首市', 3, '4210', 6, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1855, '421083', '洪湖市', 3, '4210', 7, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1856, '421087', '松滋市', 3, '4210', 8, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1857, '421088', '监利市', 3, '4210', 9, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1858, '4211', '黄冈市', 2, '42', 10, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1859, '421102', '黄州区', 3, '4211', 1, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1860, '421121', '团风县', 3, '4211', 2, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1861, '421122', '红安县', 3, '4211', 3, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1862, '421123', '罗田县', 3, '4211', 4, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1863, '421124', '英山县', 3, '4211', 5, 1, '2025-10-02 02:34:57', '2025-10-02 02:34:57');
INSERT INTO `china_region` VALUES (1864, '421125', '浠水县', 3, '4211', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1865, '421126', '蕲春县', 3, '4211', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1866, '421127', '黄梅县', 3, '4211', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1867, '421171', '龙感湖管理区', 3, '4211', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1868, '421181', '麻城市', 3, '4211', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1869, '421182', '武穴市', 3, '4211', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1870, '4212', '咸宁市', 2, '42', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1871, '421202', '咸安区', 3, '4212', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1872, '421221', '嘉鱼县', 3, '4212', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1873, '421222', '通城县', 3, '4212', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1874, '421223', '崇阳县', 3, '4212', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1875, '421224', '通山县', 3, '4212', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1876, '421281', '赤壁市', 3, '4212', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1877, '4213', '随州市', 2, '42', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1878, '421303', '曾都区', 3, '4213', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1879, '421321', '随县', 3, '4213', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1880, '421381', '广水市', 3, '4213', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1881, '4228', '恩施土家族苗族自治州', 2, '42', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1882, '422801', '恩施市', 3, '4228', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1883, '422802', '利川市', 3, '4228', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1884, '422822', '建始县', 3, '4228', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1885, '422823', '巴东县', 3, '4228', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1886, '422825', '宣恩县', 3, '4228', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1887, '422826', '咸丰县', 3, '4228', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1888, '422827', '来凤县', 3, '4228', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1889, '422828', '鹤峰县', 3, '4228', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1890, '4290', '省直辖县级行政区划', 2, '42', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1891, '429004', '仙桃市', 3, '4290', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1892, '429005', '潜江市', 3, '4290', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1893, '429006', '天门市', 3, '4290', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1894, '429021', '神农架林区', 3, '4290', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1895, '43', '湖南省', 1, NULL, 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1896, '4301', '长沙市', 2, '43', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1897, '430102', '芙蓉区', 3, '4301', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1898, '430103', '天心区', 3, '4301', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1899, '430104', '岳麓区', 3, '4301', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1900, '430105', '开福区', 3, '4301', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1901, '430111', '雨花区', 3, '4301', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1902, '430112', '望城区', 3, '4301', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1903, '430121', '长沙县', 3, '4301', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1904, '430181', '浏阳市', 3, '4301', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1905, '430182', '宁乡市', 3, '4301', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1906, '4302', '株洲市', 2, '43', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1907, '430202', '荷塘区', 3, '4302', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1908, '430203', '芦淞区', 3, '4302', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1909, '430204', '石峰区', 3, '4302', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1910, '430211', '天元区', 3, '4302', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1911, '430212', '渌口区', 3, '4302', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1912, '430223', '攸县', 3, '4302', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1913, '430224', '茶陵县', 3, '4302', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1914, '430225', '炎陵县', 3, '4302', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1915, '430281', '醴陵市', 3, '4302', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1916, '4303', '湘潭市', 2, '43', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1917, '430302', '雨湖区', 3, '4303', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1918, '430304', '岳塘区', 3, '4303', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1919, '430321', '湘潭县', 3, '4303', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1920, '430371', '湖南湘潭高新技术产业园区', 3, '4303', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1921, '430372', '湘潭昭山示范区', 3, '4303', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1922, '430373', '湘潭九华示范区', 3, '4303', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1923, '430381', '湘乡市', 3, '4303', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1924, '430382', '韶山市', 3, '4303', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1925, '4304', '衡阳市', 2, '43', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1926, '430405', '珠晖区', 3, '4304', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1927, '430406', '雁峰区', 3, '4304', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1928, '430407', '石鼓区', 3, '4304', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1929, '430408', '蒸湘区', 3, '4304', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1930, '430412', '南岳区', 3, '4304', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1931, '430421', '衡阳县', 3, '4304', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1932, '430422', '衡南县', 3, '4304', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1933, '430423', '衡山县', 3, '4304', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1934, '430424', '衡东县', 3, '4304', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1935, '430426', '祁东县', 3, '4304', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1936, '430473', '湖南衡阳松木经济开发区', 3, '4304', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1937, '430476', '湖南衡阳高新技术产业园区', 3, '4304', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1938, '430481', '耒阳市', 3, '4304', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1939, '430482', '常宁市', 3, '4304', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1940, '4305', '邵阳市', 2, '43', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1941, '430502', '双清区', 3, '4305', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1942, '430503', '大祥区', 3, '4305', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1943, '430511', '北塔区', 3, '4305', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1944, '430522', '新邵县', 3, '4305', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1945, '430523', '邵阳县', 3, '4305', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1946, '430524', '隆回县', 3, '4305', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1947, '430525', '洞口县', 3, '4305', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1948, '430527', '绥宁县', 3, '4305', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1949, '430528', '新宁县', 3, '4305', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1950, '430529', '城步苗族自治县', 3, '4305', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1951, '430581', '武冈市', 3, '4305', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1952, '430582', '邵东市', 3, '4305', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1953, '4306', '岳阳市', 2, '43', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1954, '430602', '岳阳楼区', 3, '4306', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1955, '430603', '云溪区', 3, '4306', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1956, '430611', '君山区', 3, '4306', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1957, '430621', '岳阳县', 3, '4306', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1958, '430623', '华容县', 3, '4306', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1959, '430624', '湘阴县', 3, '4306', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1960, '430626', '平江县', 3, '4306', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1961, '430671', '岳阳市屈原管理区', 3, '4306', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1962, '430681', '汨罗市', 3, '4306', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1963, '430682', '临湘市', 3, '4306', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1964, '4307', '常德市', 2, '43', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1965, '430702', '武陵区', 3, '4307', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1966, '430703', '鼎城区', 3, '4307', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1967, '430721', '安乡县', 3, '4307', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1968, '430722', '汉寿县', 3, '4307', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1969, '430723', '澧县', 3, '4307', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1970, '430724', '临澧县', 3, '4307', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1971, '430725', '桃源县', 3, '4307', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1972, '430726', '石门县', 3, '4307', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1973, '430771', '常德市西洞庭管理区', 3, '4307', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1974, '430781', '津市市', 3, '4307', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1975, '4308', '张家界市', 2, '43', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1976, '430802', '永定区', 3, '4308', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1977, '430811', '武陵源区', 3, '4308', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1978, '430821', '慈利县', 3, '4308', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1979, '430822', '桑植县', 3, '4308', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1980, '4309', '益阳市', 2, '43', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1981, '430902', '资阳区', 3, '4309', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1982, '430903', '赫山区', 3, '4309', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1983, '430921', '南县', 3, '4309', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1984, '430922', '桃江县', 3, '4309', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1985, '430923', '安化县', 3, '4309', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1986, '430971', '益阳市大通湖管理区', 3, '4309', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1987, '430972', '湖南益阳高新技术产业园区', 3, '4309', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1988, '430981', '沅江市', 3, '4309', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1989, '4310', '郴州市', 2, '43', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1990, '431002', '北湖区', 3, '4310', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1991, '431003', '苏仙区', 3, '4310', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1992, '431021', '桂阳县', 3, '4310', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1993, '431022', '宜章县', 3, '4310', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1994, '431023', '永兴县', 3, '4310', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1995, '431024', '嘉禾县', 3, '4310', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1996, '431025', '临武县', 3, '4310', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1997, '431026', '汝城县', 3, '4310', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1998, '431027', '桂东县', 3, '4310', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (1999, '431028', '安仁县', 3, '4310', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2000, '431081', '资兴市', 3, '4310', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2001, '4311', '永州市', 2, '43', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2002, '431102', '零陵区', 3, '4311', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2003, '431103', '冷水滩区', 3, '4311', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2004, '431122', '东安县', 3, '4311', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2005, '431123', '双牌县', 3, '4311', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2006, '431124', '道县', 3, '4311', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2007, '431125', '江永县', 3, '4311', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2008, '431126', '宁远县', 3, '4311', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2009, '431127', '蓝山县', 3, '4311', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2010, '431128', '新田县', 3, '4311', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2011, '431129', '江华瑶族自治县', 3, '4311', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2012, '431171', '永州经济技术开发区', 3, '4311', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2013, '431173', '永州市回龙圩管理区', 3, '4311', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2014, '431181', '祁阳市', 3, '4311', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2015, '4312', '怀化市', 2, '43', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2016, '431202', '鹤城区', 3, '4312', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2017, '431221', '中方县', 3, '4312', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2018, '431222', '沅陵县', 3, '4312', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2019, '431223', '辰溪县', 3, '4312', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2020, '431224', '溆浦县', 3, '4312', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2021, '431225', '会同县', 3, '4312', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2022, '431226', '麻阳苗族自治县', 3, '4312', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2023, '431227', '新晃侗族自治县', 3, '4312', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2024, '431228', '芷江侗族自治县', 3, '4312', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2025, '431229', '靖州苗族侗族自治县', 3, '4312', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2026, '431230', '通道侗族自治县', 3, '4312', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2027, '431271', '怀化市洪江管理区', 3, '4312', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2028, '431281', '洪江市', 3, '4312', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2029, '4313', '娄底市', 2, '43', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2030, '431302', '娄星区', 3, '4313', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2031, '431321', '双峰县', 3, '4313', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2032, '431322', '新化县', 3, '4313', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2033, '431381', '冷水江市', 3, '4313', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2034, '431382', '涟源市', 3, '4313', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2035, '4331', '湘西土家族苗族自治州', 2, '43', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2036, '433101', '吉首市', 3, '4331', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2037, '433122', '泸溪县', 3, '4331', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2038, '433123', '凤凰县', 3, '4331', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2039, '433124', '花垣县', 3, '4331', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2040, '433125', '保靖县', 3, '4331', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2041, '433126', '古丈县', 3, '4331', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2042, '433127', '永顺县', 3, '4331', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2043, '433130', '龙山县', 3, '4331', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2044, '44', '广东省', 1, NULL, 19, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2045, '4401', '广州市', 2, '44', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2046, '440103', '荔湾区', 3, '4401', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2047, '440104', '越秀区', 3, '4401', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2048, '440105', '海珠区', 3, '4401', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2049, '440106', '天河区', 3, '4401', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2050, '440111', '白云区', 3, '4401', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2051, '440112', '黄埔区', 3, '4401', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2052, '440113', '番禺区', 3, '4401', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2053, '440114', '花都区', 3, '4401', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2054, '440115', '南沙区', 3, '4401', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2055, '440117', '从化区', 3, '4401', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2056, '440118', '增城区', 3, '4401', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2057, '4402', '韶关市', 2, '44', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2058, '440203', '武江区', 3, '4402', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2059, '440204', '浈江区', 3, '4402', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2060, '440205', '曲江区', 3, '4402', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2061, '440222', '始兴县', 3, '4402', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2062, '440224', '仁化县', 3, '4402', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2063, '440229', '翁源县', 3, '4402', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2064, '440232', '乳源瑶族自治县', 3, '4402', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2065, '440233', '新丰县', 3, '4402', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2066, '440281', '乐昌市', 3, '4402', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2067, '440282', '南雄市', 3, '4402', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2068, '4403', '深圳市', 2, '44', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2069, '440303', '罗湖区', 3, '4403', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2070, '440304', '福田区', 3, '4403', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2071, '440305', '南山区', 3, '4403', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2072, '440306', '宝安区', 3, '4403', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2073, '440307', '龙岗区', 3, '4403', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2074, '440308', '盐田区', 3, '4403', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2075, '440309', '龙华区', 3, '4403', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2076, '440310', '坪山区', 3, '4403', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2077, '440311', '光明区', 3, '4403', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2078, '4404', '珠海市', 2, '44', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2079, '440402', '香洲区', 3, '4404', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2080, '440403', '斗门区', 3, '4404', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2081, '440404', '金湾区', 3, '4404', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2082, '4405', '汕头市', 2, '44', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2083, '440507', '龙湖区', 3, '4405', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2084, '440511', '金平区', 3, '4405', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2085, '440512', '濠江区', 3, '4405', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2086, '440513', '潮阳区', 3, '4405', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2087, '440514', '潮南区', 3, '4405', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2088, '440515', '澄海区', 3, '4405', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2089, '440523', '南澳县', 3, '4405', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2090, '4406', '佛山市', 2, '44', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2091, '440604', '禅城区', 3, '4406', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2092, '440605', '南海区', 3, '4406', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2093, '440606', '顺德区', 3, '4406', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2094, '440607', '三水区', 3, '4406', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2095, '440608', '高明区', 3, '4406', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2096, '4407', '江门市', 2, '44', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2097, '440703', '蓬江区', 3, '4407', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2098, '440704', '江海区', 3, '4407', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2099, '440705', '新会区', 3, '4407', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2100, '440781', '台山市', 3, '4407', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2101, '440783', '开平市', 3, '4407', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2102, '440784', '鹤山市', 3, '4407', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2103, '440785', '恩平市', 3, '4407', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2104, '4408', '湛江市', 2, '44', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2105, '440802', '赤坎区', 3, '4408', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2106, '440803', '霞山区', 3, '4408', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2107, '440804', '坡头区', 3, '4408', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2108, '440811', '麻章区', 3, '4408', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2109, '440823', '遂溪县', 3, '4408', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2110, '440825', '徐闻县', 3, '4408', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2111, '440881', '廉江市', 3, '4408', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2112, '440882', '雷州市', 3, '4408', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2113, '440883', '吴川市', 3, '4408', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2114, '4409', '茂名市', 2, '44', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2115, '440902', '茂南区', 3, '4409', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2116, '440904', '电白区', 3, '4409', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2117, '440981', '高州市', 3, '4409', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2118, '440982', '化州市', 3, '4409', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2119, '440983', '信宜市', 3, '4409', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2120, '4412', '肇庆市', 2, '44', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2121, '441202', '端州区', 3, '4412', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2122, '441203', '鼎湖区', 3, '4412', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2123, '441204', '高要区', 3, '4412', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2124, '441223', '广宁县', 3, '4412', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2125, '441224', '怀集县', 3, '4412', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2126, '441225', '封开县', 3, '4412', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2127, '441226', '德庆县', 3, '4412', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2128, '441284', '四会市', 3, '4412', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2129, '4413', '惠州市', 2, '44', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2130, '441302', '惠城区', 3, '4413', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2131, '441303', '惠阳区', 3, '4413', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2132, '441322', '博罗县', 3, '4413', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2133, '441323', '惠东县', 3, '4413', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2134, '441324', '龙门县', 3, '4413', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2135, '4414', '梅州市', 2, '44', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2136, '441402', '梅江区', 3, '4414', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2137, '441403', '梅县区', 3, '4414', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2138, '441422', '大埔县', 3, '4414', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2139, '441423', '丰顺县', 3, '4414', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2140, '441424', '五华县', 3, '4414', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2141, '441426', '平远县', 3, '4414', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2142, '441427', '蕉岭县', 3, '4414', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2143, '441481', '兴宁市', 3, '4414', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2144, '4415', '汕尾市', 2, '44', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2145, '441502', '城区', 3, '4415', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2146, '441521', '海丰县', 3, '4415', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2147, '441523', '陆河县', 3, '4415', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2148, '441581', '陆丰市', 3, '4415', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2149, '4416', '河源市', 2, '44', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2150, '441602', '源城区', 3, '4416', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2151, '441621', '紫金县', 3, '4416', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2152, '441622', '龙川县', 3, '4416', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2153, '441623', '连平县', 3, '4416', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2154, '441624', '和平县', 3, '4416', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2155, '441625', '东源县', 3, '4416', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2156, '4417', '阳江市', 2, '44', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2157, '441702', '江城区', 3, '4417', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2158, '441704', '阳东区', 3, '4417', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2159, '441721', '阳西县', 3, '4417', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2160, '441781', '阳春市', 3, '4417', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2161, '4418', '清远市', 2, '44', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2162, '441802', '清城区', 3, '4418', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2163, '441803', '清新区', 3, '4418', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2164, '441821', '佛冈县', 3, '4418', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2165, '441823', '阳山县', 3, '4418', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2166, '441825', '连山壮族瑶族自治县', 3, '4418', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2167, '441826', '连南瑶族自治县', 3, '4418', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2168, '441881', '英德市', 3, '4418', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2169, '441882', '连州市', 3, '4418', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2170, '4419', '东莞市', 2, '44', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2171, '441900003', '东城街道', 3, '4419', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2172, '441900004', '南城街道', 3, '4419', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2173, '441900005', '万江街道', 3, '4419', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2174, '441900006', '莞城街道', 3, '4419', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2175, '441900101', '石碣镇', 3, '4419', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2176, '441900102', '石龙镇', 3, '4419', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2177, '441900103', '茶山镇', 3, '4419', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2178, '441900104', '石排镇', 3, '4419', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2179, '441900105', '企石镇', 3, '4419', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2180, '441900106', '横沥镇', 3, '4419', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2181, '441900107', '桥头镇', 3, '4419', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2182, '441900108', '谢岗镇', 3, '4419', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2183, '441900109', '东坑镇', 3, '4419', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2184, '441900110', '常平镇', 3, '4419', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2185, '441900111', '寮步镇', 3, '4419', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2186, '441900112', '樟木头镇', 3, '4419', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2187, '441900113', '大朗镇', 3, '4419', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2188, '441900114', '黄江镇', 3, '4419', 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2189, '441900115', '清溪镇', 3, '4419', 19, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2190, '441900116', '塘厦镇', 3, '4419', 20, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2191, '441900117', '凤岗镇', 3, '4419', 21, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2192, '441900118', '大岭山镇', 3, '4419', 22, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2193, '441900119', '长安镇', 3, '4419', 23, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2194, '441900121', '虎门镇', 3, '4419', 24, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2195, '441900122', '厚街镇', 3, '4419', 25, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2196, '441900123', '沙田镇', 3, '4419', 26, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2197, '441900124', '道滘镇', 3, '4419', 27, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2198, '441900125', '洪梅镇', 3, '4419', 28, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2199, '441900126', '麻涌镇', 3, '4419', 29, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2200, '441900127', '望牛墩镇', 3, '4419', 30, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2201, '441900128', '中堂镇', 3, '4419', 31, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2202, '441900129', '高埗镇', 3, '4419', 32, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2203, '441900401', '松山湖', 3, '4419', 33, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2204, '441900402', '东莞港', 3, '4419', 34, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2205, '441900403', '东莞生态园', 3, '4419', 35, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2206, '441900404', '东莞滨海湾新区', 3, '4419', 36, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2207, '4420', '中山市', 2, '44', 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2208, '442000001', '石岐街道', 3, '4420', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2209, '442000002', '东区街道', 3, '4420', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2210, '442000003', '中山港街道', 3, '4420', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2211, '442000004', '西区街道', 3, '4420', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2212, '442000005', '南区街道', 3, '4420', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2213, '442000006', '五桂山街道', 3, '4420', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2214, '442000007', '民众街道', 3, '4420', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2215, '442000008', '南朗街道', 3, '4420', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2216, '442000101', '黄圃镇', 3, '4420', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2217, '442000103', '东凤镇', 3, '4420', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2218, '442000105', '古镇镇', 3, '4420', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2219, '442000106', '沙溪镇', 3, '4420', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2220, '442000107', '坦洲镇', 3, '4420', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2221, '442000108', '港口镇', 3, '4420', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2222, '442000109', '三角镇', 3, '4420', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2223, '442000110', '横栏镇', 3, '4420', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2224, '442000111', '南头镇', 3, '4420', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2225, '442000112', '阜沙镇', 3, '4420', 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2226, '442000114', '三乡镇', 3, '4420', 19, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2227, '442000115', '板芙镇', 3, '4420', 20, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2228, '442000116', '大涌镇', 3, '4420', 21, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2229, '442000117', '神湾镇', 3, '4420', 22, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2230, '442000118', '小榄镇', 3, '4420', 23, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2231, '4451', '潮州市', 2, '44', 19, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2232, '445102', '湘桥区', 3, '4451', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2233, '445103', '潮安区', 3, '4451', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2234, '445122', '饶平县', 3, '4451', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2235, '4452', '揭阳市', 2, '44', 20, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2236, '445202', '榕城区', 3, '4452', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2237, '445203', '揭东区', 3, '4452', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2238, '445222', '揭西县', 3, '4452', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2239, '445224', '惠来县', 3, '4452', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2240, '445281', '普宁市', 3, '4452', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2241, '4453', '云浮市', 2, '44', 21, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2242, '445302', '云城区', 3, '4453', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2243, '445303', '云安区', 3, '4453', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2244, '445321', '新兴县', 3, '4453', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2245, '445322', '郁南县', 3, '4453', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2246, '445381', '罗定市', 3, '4453', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2247, '45', '广西壮族自治区', 1, NULL, 20, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2248, '4501', '南宁市', 2, '45', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2249, '450102', '兴宁区', 3, '4501', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2250, '450103', '青秀区', 3, '4501', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2251, '450105', '江南区', 3, '4501', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2252, '450107', '西乡塘区', 3, '4501', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2253, '450108', '良庆区', 3, '4501', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2254, '450109', '邕宁区', 3, '4501', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2255, '450110', '武鸣区', 3, '4501', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2256, '450123', '隆安县', 3, '4501', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2257, '450124', '马山县', 3, '4501', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2258, '450125', '上林县', 3, '4501', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2259, '450126', '宾阳县', 3, '4501', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2260, '450181', '横州市', 3, '4501', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2261, '4502', '柳州市', 2, '45', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2262, '450202', '城中区', 3, '4502', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2263, '450203', '鱼峰区', 3, '4502', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2264, '450204', '柳南区', 3, '4502', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2265, '450205', '柳北区', 3, '4502', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2266, '450206', '柳江区', 3, '4502', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2267, '450222', '柳城县', 3, '4502', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2268, '450223', '鹿寨县', 3, '4502', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2269, '450224', '融安县', 3, '4502', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2270, '450225', '融水苗族自治县', 3, '4502', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2271, '450226', '三江侗族自治县', 3, '4502', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2272, '4503', '桂林市', 2, '45', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2273, '450302', '秀峰区', 3, '4503', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2274, '450303', '叠彩区', 3, '4503', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2275, '450304', '象山区', 3, '4503', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2276, '450305', '七星区', 3, '4503', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2277, '450311', '雁山区', 3, '4503', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2278, '450312', '临桂区', 3, '4503', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2279, '450321', '阳朔县', 3, '4503', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2280, '450323', '灵川县', 3, '4503', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2281, '450324', '全州县', 3, '4503', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2282, '450325', '兴安县', 3, '4503', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2283, '450326', '永福县', 3, '4503', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2284, '450327', '灌阳县', 3, '4503', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2285, '450328', '龙胜各族自治县', 3, '4503', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2286, '450329', '资源县', 3, '4503', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2287, '450330', '平乐县', 3, '4503', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2288, '450332', '恭城瑶族自治县', 3, '4503', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2289, '450381', '荔浦市', 3, '4503', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2290, '4504', '梧州市', 2, '45', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2291, '450403', '万秀区', 3, '4504', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2292, '450405', '长洲区', 3, '4504', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2293, '450406', '龙圩区', 3, '4504', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2294, '450421', '苍梧县', 3, '4504', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2295, '450422', '藤县', 3, '4504', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2296, '450423', '蒙山县', 3, '4504', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2297, '450481', '岑溪市', 3, '4504', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2298, '4505', '北海市', 2, '45', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2299, '450502', '海城区', 3, '4505', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2300, '450503', '银海区', 3, '4505', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2301, '450512', '铁山港区', 3, '4505', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2302, '450521', '合浦县', 3, '4505', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2303, '4506', '防城港市', 2, '45', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2304, '450602', '港口区', 3, '4506', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2305, '450603', '防城区', 3, '4506', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2306, '450621', '上思县', 3, '4506', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2307, '450681', '东兴市', 3, '4506', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2308, '4507', '钦州市', 2, '45', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2309, '450702', '钦南区', 3, '4507', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2310, '450703', '钦北区', 3, '4507', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2311, '450721', '灵山县', 3, '4507', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2312, '450722', '浦北县', 3, '4507', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2313, '4508', '贵港市', 2, '45', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2314, '450802', '港北区', 3, '4508', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2315, '450803', '港南区', 3, '4508', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2316, '450804', '覃塘区', 3, '4508', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2317, '450821', '平南县', 3, '4508', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2318, '450881', '桂平市', 3, '4508', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2319, '4509', '玉林市', 2, '45', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2320, '450902', '玉州区', 3, '4509', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2321, '450903', '福绵区', 3, '4509', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2322, '450921', '容县', 3, '4509', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2323, '450922', '陆川县', 3, '4509', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2324, '450923', '博白县', 3, '4509', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2325, '450924', '兴业县', 3, '4509', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2326, '450981', '北流市', 3, '4509', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2327, '4510', '百色市', 2, '45', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2328, '451002', '右江区', 3, '4510', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2329, '451003', '田阳区', 3, '4510', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2330, '451022', '田东县', 3, '4510', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2331, '451024', '德保县', 3, '4510', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2332, '451026', '那坡县', 3, '4510', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2333, '451027', '凌云县', 3, '4510', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2334, '451028', '乐业县', 3, '4510', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2335, '451029', '田林县', 3, '4510', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2336, '451030', '西林县', 3, '4510', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2337, '451031', '隆林各族自治县', 3, '4510', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2338, '451081', '靖西市', 3, '4510', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2339, '451082', '平果市', 3, '4510', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2340, '4511', '贺州市', 2, '45', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2341, '451102', '八步区', 3, '4511', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2342, '451103', '平桂区', 3, '4511', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2343, '451121', '昭平县', 3, '4511', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2344, '451122', '钟山县', 3, '4511', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2345, '451123', '富川瑶族自治县', 3, '4511', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2346, '4512', '河池市', 2, '45', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2347, '451202', '金城江区', 3, '4512', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2348, '451203', '宜州区', 3, '4512', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2349, '451221', '南丹县', 3, '4512', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2350, '451222', '天峨县', 3, '4512', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2351, '451223', '凤山县', 3, '4512', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2352, '451224', '东兰县', 3, '4512', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2353, '451225', '罗城仫佬族自治县', 3, '4512', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2354, '451226', '环江毛南族自治县', 3, '4512', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2355, '451227', '巴马瑶族自治县', 3, '4512', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2356, '451228', '都安瑶族自治县', 3, '4512', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2357, '451229', '大化瑶族自治县', 3, '4512', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2358, '4513', '来宾市', 2, '45', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2359, '451302', '兴宾区', 3, '4513', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2360, '451321', '忻城县', 3, '4513', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2361, '451322', '象州县', 3, '4513', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2362, '451323', '武宣县', 3, '4513', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2363, '451324', '金秀瑶族自治县', 3, '4513', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2364, '451381', '合山市', 3, '4513', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2365, '4514', '崇左市', 2, '45', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2366, '451402', '江州区', 3, '4514', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2367, '451421', '扶绥县', 3, '4514', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2368, '451422', '宁明县', 3, '4514', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2369, '451423', '龙州县', 3, '4514', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2370, '451424', '大新县', 3, '4514', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2371, '451425', '天等县', 3, '4514', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2372, '451481', '凭祥市', 3, '4514', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2373, '46', '海南省', 1, NULL, 21, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2374, '4601', '海口市', 2, '46', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2375, '460105', '秀英区', 3, '4601', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2376, '460106', '龙华区', 3, '4601', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2377, '460107', '琼山区', 3, '4601', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2378, '460108', '美兰区', 3, '4601', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2379, '4602', '三亚市', 2, '46', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2380, '460202', '海棠区', 3, '4602', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2381, '460203', '吉阳区', 3, '4602', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2382, '460204', '天涯区', 3, '4602', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2383, '460205', '崖州区', 3, '4602', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2384, '4603', '三沙市', 2, '46', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2385, '460321', '西沙群岛', 3, '4603', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2386, '460322', '南沙群岛', 3, '4603', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2387, '460323', '中沙群岛的岛礁及其海域', 3, '4603', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2388, '4604', '儋州市', 2, '46', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2389, '460400100', '那大镇', 3, '4604', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2390, '460400101', '和庆镇', 3, '4604', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2391, '460400102', '南丰镇', 3, '4604', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2392, '460400103', '大成镇', 3, '4604', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2393, '460400104', '雅星镇', 3, '4604', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2394, '460400105', '兰洋镇', 3, '4604', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2395, '460400106', '光村镇', 3, '4604', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2396, '460400107', '木棠镇', 3, '4604', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2397, '460400108', '海头镇', 3, '4604', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2398, '460400109', '峨蔓镇', 3, '4604', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2399, '460400111', '王五镇', 3, '4604', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2400, '460400112', '白马井镇', 3, '4604', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2401, '460400113', '中和镇', 3, '4604', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2402, '460400114', '排浦镇', 3, '4604', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2403, '460400115', '东成镇', 3, '4604', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2404, '460400116', '新州镇', 3, '4604', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2405, '460400499', '洋浦经济开发区', 3, '4604', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2406, '460400500', '华南热作学院', 3, '4604', 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2407, '4690', '省直辖县级行政区划', 2, '46', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2408, '469001', '五指山市', 3, '4690', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2409, '469002', '琼海市', 3, '4690', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2410, '469005', '文昌市', 3, '4690', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2411, '469006', '万宁市', 3, '4690', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2412, '469007', '东方市', 3, '4690', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2413, '469021', '定安县', 3, '4690', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2414, '469022', '屯昌县', 3, '4690', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2415, '469023', '澄迈县', 3, '4690', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2416, '469024', '临高县', 3, '4690', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2417, '469025', '白沙黎族自治县', 3, '4690', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2418, '469026', '昌江黎族自治县', 3, '4690', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2419, '469027', '乐东黎族自治县', 3, '4690', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2420, '469028', '陵水黎族自治县', 3, '4690', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2421, '469029', '保亭黎族苗族自治县', 3, '4690', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2422, '469030', '琼中黎族苗族自治县', 3, '4690', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2423, '50', '重庆市', 1, NULL, 22, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2424, '5001', '市辖区', 2, '50', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2425, '500101', '万州区', 3, '5001', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2426, '500102', '涪陵区', 3, '5001', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2427, '500103', '渝中区', 3, '5001', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2428, '500104', '大渡口区', 3, '5001', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2429, '500105', '江北区', 3, '5001', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2430, '500106', '沙坪坝区', 3, '5001', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2431, '500107', '九龙坡区', 3, '5001', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2432, '500108', '南岸区', 3, '5001', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2433, '500109', '北碚区', 3, '5001', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2434, '500110', '綦江区', 3, '5001', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2435, '500111', '大足区', 3, '5001', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2436, '500112', '渝北区', 3, '5001', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2437, '500113', '巴南区', 3, '5001', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2438, '500114', '黔江区', 3, '5001', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2439, '500115', '长寿区', 3, '5001', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2440, '500116', '江津区', 3, '5001', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2441, '500117', '合川区', 3, '5001', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2442, '500118', '永川区', 3, '5001', 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2443, '500119', '南川区', 3, '5001', 19, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2444, '500120', '璧山区', 3, '5001', 20, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2445, '500151', '铜梁区', 3, '5001', 21, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2446, '500152', '潼南区', 3, '5001', 22, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2447, '500153', '荣昌区', 3, '5001', 23, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2448, '500154', '开州区', 3, '5001', 24, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2449, '500155', '梁平区', 3, '5001', 25, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2450, '500156', '武隆区', 3, '5001', 26, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2451, '5002', '县', 2, '50', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2452, '500229', '城口县', 3, '5002', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2453, '500230', '丰都县', 3, '5002', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2454, '500231', '垫江县', 3, '5002', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2455, '500233', '忠县', 3, '5002', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2456, '500235', '云阳县', 3, '5002', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2457, '500236', '奉节县', 3, '5002', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2458, '500237', '巫山县', 3, '5002', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2459, '500238', '巫溪县', 3, '5002', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2460, '500240', '石柱土家族自治县', 3, '5002', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2461, '500241', '秀山土家族苗族自治县', 3, '5002', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2462, '500242', '酉阳土家族苗族自治县', 3, '5002', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2463, '500243', '彭水苗族土家族自治县', 3, '5002', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2464, '51', '四川省', 1, NULL, 23, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2465, '5101', '成都市', 2, '51', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2466, '510104', '锦江区', 3, '5101', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2467, '510105', '青羊区', 3, '5101', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2468, '510106', '金牛区', 3, '5101', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2469, '510107', '武侯区', 3, '5101', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2470, '510108', '成华区', 3, '5101', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2471, '510112', '龙泉驿区', 3, '5101', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2472, '510113', '青白江区', 3, '5101', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2473, '510114', '新都区', 3, '5101', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2474, '510115', '温江区', 3, '5101', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2475, '510116', '双流区', 3, '5101', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2476, '510117', '郫都区', 3, '5101', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2477, '510118', '新津区', 3, '5101', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2478, '510121', '金堂县', 3, '5101', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2479, '510129', '大邑县', 3, '5101', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2480, '510131', '蒲江县', 3, '5101', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2481, '510181', '都江堰市', 3, '5101', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2482, '510182', '彭州市', 3, '5101', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2483, '510183', '邛崃市', 3, '5101', 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2484, '510184', '崇州市', 3, '5101', 19, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2485, '510185', '简阳市', 3, '5101', 20, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2486, '5103', '自贡市', 2, '51', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2487, '510302', '自流井区', 3, '5103', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2488, '510303', '贡井区', 3, '5103', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2489, '510304', '大安区', 3, '5103', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2490, '510311', '沿滩区', 3, '5103', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2491, '510321', '荣县', 3, '5103', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2492, '510322', '富顺县', 3, '5103', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2493, '5104', '攀枝花市', 2, '51', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2494, '510402', '东区', 3, '5104', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2495, '510403', '西区', 3, '5104', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2496, '510411', '仁和区', 3, '5104', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2497, '510421', '米易县', 3, '5104', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2498, '510422', '盐边县', 3, '5104', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2499, '5105', '泸州市', 2, '51', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2500, '510502', '江阳区', 3, '5105', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2501, '510503', '纳溪区', 3, '5105', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2502, '510504', '龙马潭区', 3, '5105', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2503, '510521', '泸县', 3, '5105', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2504, '510522', '合江县', 3, '5105', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2505, '510524', '叙永县', 3, '5105', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2506, '510525', '古蔺县', 3, '5105', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2507, '5106', '德阳市', 2, '51', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2508, '510603', '旌阳区', 3, '5106', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2509, '510604', '罗江区', 3, '5106', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2510, '510623', '中江县', 3, '5106', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2511, '510681', '广汉市', 3, '5106', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2512, '510682', '什邡市', 3, '5106', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2513, '510683', '绵竹市', 3, '5106', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2514, '5107', '绵阳市', 2, '51', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2515, '510703', '涪城区', 3, '5107', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2516, '510704', '游仙区', 3, '5107', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2517, '510705', '安州区', 3, '5107', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2518, '510722', '三台县', 3, '5107', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2519, '510723', '盐亭县', 3, '5107', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2520, '510725', '梓潼县', 3, '5107', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2521, '510726', '北川羌族自治县', 3, '5107', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2522, '510727', '平武县', 3, '5107', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2523, '510781', '江油市', 3, '5107', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2524, '5108', '广元市', 2, '51', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2525, '510802', '利州区', 3, '5108', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2526, '510811', '昭化区', 3, '5108', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2527, '510812', '朝天区', 3, '5108', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2528, '510821', '旺苍县', 3, '5108', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2529, '510822', '青川县', 3, '5108', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2530, '510823', '剑阁县', 3, '5108', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2531, '510824', '苍溪县', 3, '5108', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2532, '5109', '遂宁市', 2, '51', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2533, '510903', '船山区', 3, '5109', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2534, '510904', '安居区', 3, '5109', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2535, '510921', '蓬溪县', 3, '5109', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2536, '510923', '大英县', 3, '5109', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2537, '510981', '射洪市', 3, '5109', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2538, '5110', '内江市', 2, '51', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2539, '511002', '市中区', 3, '5110', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2540, '511011', '东兴区', 3, '5110', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2541, '511024', '威远县', 3, '5110', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2542, '511025', '资中县', 3, '5110', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2543, '511083', '隆昌市', 3, '5110', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2544, '5111', '乐山市', 2, '51', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2545, '511102', '市中区', 3, '5111', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2546, '511111', '沙湾区', 3, '5111', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2547, '511112', '五通桥区', 3, '5111', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2548, '511113', '金口河区', 3, '5111', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2549, '511123', '犍为县', 3, '5111', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2550, '511124', '井研县', 3, '5111', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2551, '511126', '夹江县', 3, '5111', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2552, '511129', '沐川县', 3, '5111', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2553, '511132', '峨边彝族自治县', 3, '5111', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2554, '511133', '马边彝族自治县', 3, '5111', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2555, '511181', '峨眉山市', 3, '5111', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2556, '5113', '南充市', 2, '51', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2557, '511302', '顺庆区', 3, '5113', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2558, '511303', '高坪区', 3, '5113', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2559, '511304', '嘉陵区', 3, '5113', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2560, '511321', '南部县', 3, '5113', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2561, '511322', '营山县', 3, '5113', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2562, '511323', '蓬安县', 3, '5113', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2563, '511324', '仪陇县', 3, '5113', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2564, '511325', '西充县', 3, '5113', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2565, '511381', '阆中市', 3, '5113', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2566, '5114', '眉山市', 2, '51', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2567, '511402', '东坡区', 3, '5114', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2568, '511403', '彭山区', 3, '5114', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2569, '511421', '仁寿县', 3, '5114', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2570, '511423', '洪雅县', 3, '5114', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2571, '511424', '丹棱县', 3, '5114', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2572, '511425', '青神县', 3, '5114', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2573, '5115', '宜宾市', 2, '51', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2574, '511502', '翠屏区', 3, '5115', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2575, '511503', '南溪区', 3, '5115', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2576, '511504', '叙州区', 3, '5115', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2577, '511523', '江安县', 3, '5115', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2578, '511524', '长宁县', 3, '5115', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2579, '511525', '高县', 3, '5115', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2580, '511526', '珙县', 3, '5115', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2581, '511527', '筠连县', 3, '5115', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2582, '511528', '兴文县', 3, '5115', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2583, '511529', '屏山县', 3, '5115', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2584, '5116', '广安市', 2, '51', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2585, '511602', '广安区', 3, '5116', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2586, '511603', '前锋区', 3, '5116', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2587, '511621', '岳池县', 3, '5116', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2588, '511622', '武胜县', 3, '5116', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2589, '511623', '邻水县', 3, '5116', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2590, '511681', '华蓥市', 3, '5116', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2591, '5117', '达州市', 2, '51', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2592, '511702', '通川区', 3, '5117', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2593, '511703', '达川区', 3, '5117', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2594, '511722', '宣汉县', 3, '5117', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2595, '511723', '开江县', 3, '5117', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2596, '511724', '大竹县', 3, '5117', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2597, '511725', '渠县', 3, '5117', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2598, '511781', '万源市', 3, '5117', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2599, '5118', '雅安市', 2, '51', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2600, '511802', '雨城区', 3, '5118', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2601, '511803', '名山区', 3, '5118', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2602, '511822', '荥经县', 3, '5118', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2603, '511823', '汉源县', 3, '5118', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2604, '511824', '石棉县', 3, '5118', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2605, '511825', '天全县', 3, '5118', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2606, '511826', '芦山县', 3, '5118', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2607, '511827', '宝兴县', 3, '5118', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2608, '5119', '巴中市', 2, '51', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2609, '511902', '巴州区', 3, '5119', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2610, '511903', '恩阳区', 3, '5119', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2611, '511921', '通江县', 3, '5119', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2612, '511922', '南江县', 3, '5119', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2613, '511923', '平昌县', 3, '5119', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2614, '5120', '资阳市', 2, '51', 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2615, '512002', '雁江区', 3, '5120', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2616, '512021', '安岳县', 3, '5120', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2617, '512022', '乐至县', 3, '5120', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2618, '5132', '阿坝藏族羌族自治州', 2, '51', 19, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2619, '513201', '马尔康市', 3, '5132', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2620, '513221', '汶川县', 3, '5132', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2621, '513222', '理县', 3, '5132', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2622, '513223', '茂县', 3, '5132', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2623, '513224', '松潘县', 3, '5132', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2624, '513225', '九寨沟县', 3, '5132', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2625, '513226', '金川县', 3, '5132', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2626, '513227', '小金县', 3, '5132', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2627, '513228', '黑水县', 3, '5132', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2628, '513230', '壤塘县', 3, '5132', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2629, '513231', '阿坝县', 3, '5132', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2630, '513232', '若尔盖县', 3, '5132', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2631, '513233', '红原县', 3, '5132', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2632, '5133', '甘孜藏族自治州', 2, '51', 20, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2633, '513301', '康定市', 3, '5133', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2634, '513322', '泸定县', 3, '5133', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2635, '513323', '丹巴县', 3, '5133', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2636, '513324', '九龙县', 3, '5133', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2637, '513325', '雅江县', 3, '5133', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2638, '513326', '道孚县', 3, '5133', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2639, '513327', '炉霍县', 3, '5133', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2640, '513328', '甘孜县', 3, '5133', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2641, '513329', '新龙县', 3, '5133', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2642, '513330', '德格县', 3, '5133', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2643, '513331', '白玉县', 3, '5133', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2644, '513332', '石渠县', 3, '5133', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2645, '513333', '色达县', 3, '5133', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2646, '513334', '理塘县', 3, '5133', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2647, '513335', '巴塘县', 3, '5133', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2648, '513336', '乡城县', 3, '5133', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2649, '513337', '稻城县', 3, '5133', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2650, '513338', '得荣县', 3, '5133', 18, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2651, '5134', '凉山彝族自治州', 2, '51', 21, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2652, '513401', '西昌市', 3, '5134', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2653, '513402', '会理市', 3, '5134', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2654, '513422', '木里藏族自治县', 3, '5134', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2655, '513423', '盐源县', 3, '5134', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2656, '513424', '德昌县', 3, '5134', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2657, '513426', '会东县', 3, '5134', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2658, '513427', '宁南县', 3, '5134', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2659, '513428', '普格县', 3, '5134', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2660, '513429', '布拖县', 3, '5134', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2661, '513430', '金阳县', 3, '5134', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2662, '513431', '昭觉县', 3, '5134', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2663, '513432', '喜德县', 3, '5134', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2664, '513433', '冕宁县', 3, '5134', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2665, '513434', '越西县', 3, '5134', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2666, '513435', '甘洛县', 3, '5134', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2667, '513436', '美姑县', 3, '5134', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2668, '513437', '雷波县', 3, '5134', 17, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2669, '52', '贵州省', 1, NULL, 24, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2670, '5201', '贵阳市', 2, '52', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2671, '520102', '南明区', 3, '5201', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2672, '520103', '云岩区', 3, '5201', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2673, '520111', '花溪区', 3, '5201', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2674, '520112', '乌当区', 3, '5201', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2675, '520113', '白云区', 3, '5201', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2676, '520115', '观山湖区', 3, '5201', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2677, '520121', '开阳县', 3, '5201', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2678, '520122', '息烽县', 3, '5201', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2679, '520123', '修文县', 3, '5201', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2680, '520181', '清镇市', 3, '5201', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2681, '5202', '六盘水市', 2, '52', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2682, '520201', '钟山区', 3, '5202', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2683, '520203', '六枝特区', 3, '5202', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2684, '520204', '水城区', 3, '5202', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2685, '520281', '盘州市', 3, '5202', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2686, '5203', '遵义市', 2, '52', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2687, '520302', '红花岗区', 3, '5203', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2688, '520303', '汇川区', 3, '5203', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2689, '520304', '播州区', 3, '5203', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2690, '520322', '桐梓县', 3, '5203', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2691, '520323', '绥阳县', 3, '5203', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2692, '520324', '正安县', 3, '5203', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2693, '520325', '道真仡佬族苗族自治县', 3, '5203', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2694, '520326', '务川仡佬族苗族自治县', 3, '5203', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2695, '520327', '凤冈县', 3, '5203', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2696, '520328', '湄潭县', 3, '5203', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2697, '520329', '余庆县', 3, '5203', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2698, '520330', '习水县', 3, '5203', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2699, '520381', '赤水市', 3, '5203', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2700, '520382', '仁怀市', 3, '5203', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2701, '5204', '安顺市', 2, '52', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2702, '520402', '西秀区', 3, '5204', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2703, '520403', '平坝区', 3, '5204', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2704, '520422', '普定县', 3, '5204', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2705, '520423', '镇宁布依族苗族自治县', 3, '5204', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2706, '520424', '关岭布依族苗族自治县', 3, '5204', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2707, '520425', '紫云苗族布依族自治县', 3, '5204', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2708, '5205', '毕节市', 2, '52', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2709, '520502', '七星关区', 3, '5205', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2710, '520521', '大方县', 3, '5205', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2711, '520523', '金沙县', 3, '5205', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2712, '520524', '织金县', 3, '5205', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2713, '520525', '纳雍县', 3, '5205', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2714, '520526', '威宁彝族回族苗族自治县', 3, '5205', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2715, '520527', '赫章县', 3, '5205', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2716, '520581', '黔西市', 3, '5205', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2717, '5206', '铜仁市', 2, '52', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2718, '520602', '碧江区', 3, '5206', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2719, '520603', '万山区', 3, '5206', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2720, '520621', '江口县', 3, '5206', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2721, '520622', '玉屏侗族自治县', 3, '5206', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2722, '520623', '石阡县', 3, '5206', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2723, '520624', '思南县', 3, '5206', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2724, '520625', '印江土家族苗族自治县', 3, '5206', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2725, '520626', '德江县', 3, '5206', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2726, '520627', '沿河土家族自治县', 3, '5206', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2727, '520628', '松桃苗族自治县', 3, '5206', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2728, '5223', '黔西南布依族苗族自治州', 2, '52', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2729, '522301', '兴义市', 3, '5223', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2730, '522302', '兴仁市', 3, '5223', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2731, '522323', '普安县', 3, '5223', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2732, '522324', '晴隆县', 3, '5223', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2733, '522325', '贞丰县', 3, '5223', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2734, '522326', '望谟县', 3, '5223', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2735, '522327', '册亨县', 3, '5223', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2736, '522328', '安龙县', 3, '5223', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2737, '5226', '黔东南苗族侗族自治州', 2, '52', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2738, '522601', '凯里市', 3, '5226', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2739, '522622', '黄平县', 3, '5226', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2740, '522623', '施秉县', 3, '5226', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2741, '522624', '三穗县', 3, '5226', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2742, '522625', '镇远县', 3, '5226', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2743, '522626', '岑巩县', 3, '5226', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2744, '522627', '天柱县', 3, '5226', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2745, '522628', '锦屏县', 3, '5226', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2746, '522629', '剑河县', 3, '5226', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2747, '522630', '台江县', 3, '5226', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2748, '522631', '黎平县', 3, '5226', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2749, '522632', '榕江县', 3, '5226', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2750, '522633', '从江县', 3, '5226', 13, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2751, '522634', '雷山县', 3, '5226', 14, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2752, '522635', '麻江县', 3, '5226', 15, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2753, '522636', '丹寨县', 3, '5226', 16, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2754, '5227', '黔南布依族苗族自治州', 2, '52', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2755, '522701', '都匀市', 3, '5227', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2756, '522702', '福泉市', 3, '5227', 2, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2757, '522722', '荔波县', 3, '5227', 3, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2758, '522723', '贵定县', 3, '5227', 4, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2759, '522725', '瓮安县', 3, '5227', 5, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2760, '522726', '独山县', 3, '5227', 6, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2761, '522727', '平塘县', 3, '5227', 7, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2762, '522728', '罗甸县', 3, '5227', 8, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2763, '522729', '长顺县', 3, '5227', 9, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2764, '522730', '龙里县', 3, '5227', 10, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2765, '522731', '惠水县', 3, '5227', 11, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2766, '522732', '三都水族自治县', 3, '5227', 12, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2767, '53', '云南省', 1, NULL, 25, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2768, '5301', '昆明市', 2, '53', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2769, '530102', '五华区', 3, '5301', 1, 1, '2025-10-02 02:34:58', '2025-10-02 02:34:58');
INSERT INTO `china_region` VALUES (2770, '530103', '盘龙区', 3, '5301', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2771, '530111', '官渡区', 3, '5301', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2772, '530112', '西山区', 3, '5301', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2773, '530113', '东川区', 3, '5301', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2774, '530114', '呈贡区', 3, '5301', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2775, '530115', '晋宁区', 3, '5301', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2776, '530124', '富民县', 3, '5301', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2777, '530125', '宜良县', 3, '5301', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2778, '530126', '石林彝族自治县', 3, '5301', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2779, '530127', '嵩明县', 3, '5301', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2780, '530128', '禄劝彝族苗族自治县', 3, '5301', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2781, '530129', '寻甸回族彝族自治县', 3, '5301', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2782, '530181', '安宁市', 3, '5301', 14, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2783, '5303', '曲靖市', 2, '53', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2784, '530302', '麒麟区', 3, '5303', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2785, '530303', '沾益区', 3, '5303', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2786, '530304', '马龙区', 3, '5303', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2787, '530322', '陆良县', 3, '5303', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2788, '530323', '师宗县', 3, '5303', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2789, '530324', '罗平县', 3, '5303', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2790, '530325', '富源县', 3, '5303', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2791, '530326', '会泽县', 3, '5303', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2792, '530381', '宣威市', 3, '5303', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2793, '5304', '玉溪市', 2, '53', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2794, '530402', '红塔区', 3, '5304', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2795, '530403', '江川区', 3, '5304', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2796, '530423', '通海县', 3, '5304', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2797, '530424', '华宁县', 3, '5304', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2798, '530425', '易门县', 3, '5304', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2799, '530426', '峨山彝族自治县', 3, '5304', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2800, '530427', '新平彝族傣族自治县', 3, '5304', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2801, '530428', '元江哈尼族彝族傣族自治县', 3, '5304', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2802, '530481', '澄江市', 3, '5304', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2803, '5305', '保山市', 2, '53', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2804, '530502', '隆阳区', 3, '5305', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2805, '530521', '施甸县', 3, '5305', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2806, '530523', '龙陵县', 3, '5305', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2807, '530524', '昌宁县', 3, '5305', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2808, '530581', '腾冲市', 3, '5305', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2809, '5306', '昭通市', 2, '53', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2810, '530602', '昭阳区', 3, '5306', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2811, '530621', '鲁甸县', 3, '5306', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2812, '530622', '巧家县', 3, '5306', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2813, '530623', '盐津县', 3, '5306', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2814, '530624', '大关县', 3, '5306', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2815, '530625', '永善县', 3, '5306', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2816, '530626', '绥江县', 3, '5306', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2817, '530627', '镇雄县', 3, '5306', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2818, '530628', '彝良县', 3, '5306', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2819, '530629', '威信县', 3, '5306', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2820, '530681', '水富市', 3, '5306', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2821, '5307', '丽江市', 2, '53', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2822, '530702', '古城区', 3, '5307', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2823, '530721', '玉龙纳西族自治县', 3, '5307', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2824, '530722', '永胜县', 3, '5307', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2825, '530723', '华坪县', 3, '5307', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2826, '530724', '宁蒗彝族自治县', 3, '5307', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2827, '5308', '普洱市', 2, '53', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2828, '530802', '思茅区', 3, '5308', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2829, '530821', '宁洱哈尼族彝族自治县', 3, '5308', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2830, '530822', '墨江哈尼族自治县', 3, '5308', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2831, '530823', '景东彝族自治县', 3, '5308', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2832, '530824', '景谷傣族彝族自治县', 3, '5308', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2833, '530825', '镇沅彝族哈尼族拉祜族自治县', 3, '5308', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2834, '530826', '江城哈尼族彝族自治县', 3, '5308', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2835, '530827', '孟连傣族拉祜族佤族自治县', 3, '5308', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2836, '530828', '澜沧拉祜族自治县', 3, '5308', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2837, '530829', '西盟佤族自治县', 3, '5308', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2838, '5309', '临沧市', 2, '53', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2839, '530902', '临翔区', 3, '5309', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2840, '530921', '凤庆县', 3, '5309', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2841, '530922', '云县', 3, '5309', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2842, '530923', '永德县', 3, '5309', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2843, '530924', '镇康县', 3, '5309', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2844, '530925', '双江拉祜族佤族布朗族傣族自治县', 3, '5309', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2845, '530926', '耿马傣族佤族自治县', 3, '5309', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2846, '530927', '沧源佤族自治县', 3, '5309', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2847, '5323', '楚雄彝族自治州', 2, '53', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2848, '532301', '楚雄市', 3, '5323', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2849, '532302', '禄丰市', 3, '5323', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2850, '532322', '双柏县', 3, '5323', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2851, '532323', '牟定县', 3, '5323', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2852, '532324', '南华县', 3, '5323', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2853, '532325', '姚安县', 3, '5323', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2854, '532326', '大姚县', 3, '5323', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2855, '532327', '永仁县', 3, '5323', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2856, '532328', '元谋县', 3, '5323', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2857, '532329', '武定县', 3, '5323', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2858, '5325', '红河哈尼族彝族自治州', 2, '53', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2859, '532501', '个旧市', 3, '5325', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2860, '532502', '开远市', 3, '5325', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2861, '532503', '蒙自市', 3, '5325', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2862, '532504', '弥勒市', 3, '5325', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2863, '532523', '屏边苗族自治县', 3, '5325', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2864, '532524', '建水县', 3, '5325', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2865, '532525', '石屏县', 3, '5325', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2866, '532527', '泸西县', 3, '5325', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2867, '532528', '元阳县', 3, '5325', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2868, '532529', '红河县', 3, '5325', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2869, '532530', '金平苗族瑶族傣族自治县', 3, '5325', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2870, '532531', '绿春县', 3, '5325', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2871, '532532', '河口瑶族自治县', 3, '5325', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2872, '5326', '文山壮族苗族自治州', 2, '53', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2873, '532601', '文山市', 3, '5326', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2874, '532622', '砚山县', 3, '5326', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2875, '532623', '西畴县', 3, '5326', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2876, '532624', '麻栗坡县', 3, '5326', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2877, '532625', '马关县', 3, '5326', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2878, '532626', '丘北县', 3, '5326', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2879, '532627', '广南县', 3, '5326', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2880, '532628', '富宁县', 3, '5326', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2881, '5328', '西双版纳傣族自治州', 2, '53', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2882, '532801', '景洪市', 3, '5328', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2883, '532822', '勐海县', 3, '5328', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2884, '532823', '勐腊县', 3, '5328', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2885, '5329', '大理白族自治州', 2, '53', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2886, '532901', '大理市', 3, '5329', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2887, '532922', '漾濞彝族自治县', 3, '5329', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2888, '532923', '祥云县', 3, '5329', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2889, '532924', '宾川县', 3, '5329', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2890, '532925', '弥渡县', 3, '5329', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2891, '532926', '南涧彝族自治县', 3, '5329', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2892, '532927', '巍山彝族回族自治县', 3, '5329', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2893, '532928', '永平县', 3, '5329', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2894, '532929', '云龙县', 3, '5329', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2895, '532930', '洱源县', 3, '5329', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2896, '532931', '剑川县', 3, '5329', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2897, '532932', '鹤庆县', 3, '5329', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2898, '5331', '德宏傣族景颇族自治州', 2, '53', 14, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2899, '533102', '瑞丽市', 3, '5331', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2900, '533103', '芒市', 3, '5331', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2901, '533122', '梁河县', 3, '5331', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2902, '533123', '盈江县', 3, '5331', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2903, '533124', '陇川县', 3, '5331', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2904, '5333', '怒江傈僳族自治州', 2, '53', 15, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2905, '533301', '泸水市', 3, '5333', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2906, '533323', '福贡县', 3, '5333', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2907, '533324', '贡山独龙族怒族自治县', 3, '5333', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2908, '533325', '兰坪白族普米族自治县', 3, '5333', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2909, '5334', '迪庆藏族自治州', 2, '53', 16, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2910, '533401', '香格里拉市', 3, '5334', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2911, '533422', '德钦县', 3, '5334', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2912, '533423', '维西傈僳族自治县', 3, '5334', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2913, '54', '西藏自治区', 1, NULL, 26, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2914, '5401', '拉萨市', 2, '54', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2915, '540102', '城关区', 3, '5401', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2916, '540103', '堆龙德庆区', 3, '5401', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2917, '540104', '达孜区', 3, '5401', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2918, '540121', '林周县', 3, '5401', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2919, '540122', '当雄县', 3, '5401', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2920, '540123', '尼木县', 3, '5401', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2921, '540124', '曲水县', 3, '5401', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2922, '540127', '墨竹工卡县', 3, '5401', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2923, '540171', '格尔木藏青工业园区', 3, '5401', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2924, '540172', '拉萨经济技术开发区', 3, '5401', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2925, '540173', '西藏文化旅游创意园区', 3, '5401', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2926, '540174', '达孜工业园区', 3, '5401', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2927, '5402', '日喀则市', 2, '54', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2928, '540202', '桑珠孜区', 3, '5402', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2929, '540221', '南木林县', 3, '5402', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2930, '540222', '江孜县', 3, '5402', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2931, '540223', '定日县', 3, '5402', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2932, '540224', '萨迦县', 3, '5402', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2933, '540225', '拉孜县', 3, '5402', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2934, '540226', '昂仁县', 3, '5402', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2935, '540227', '谢通门县', 3, '5402', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2936, '540228', '白朗县', 3, '5402', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2937, '540229', '仁布县', 3, '5402', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2938, '540230', '康马县', 3, '5402', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2939, '540231', '定结县', 3, '5402', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2940, '540232', '仲巴县', 3, '5402', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2941, '540233', '亚东县', 3, '5402', 14, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2942, '540234', '吉隆县', 3, '5402', 15, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2943, '540235', '聂拉木县', 3, '5402', 16, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2944, '540236', '萨嘎县', 3, '5402', 17, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2945, '540237', '岗巴县', 3, '5402', 18, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2946, '5403', '昌都市', 2, '54', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2947, '540302', '卡若区', 3, '5403', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2948, '540321', '江达县', 3, '5403', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2949, '540322', '贡觉县', 3, '5403', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2950, '540323', '类乌齐县', 3, '5403', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2951, '540324', '丁青县', 3, '5403', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2952, '540325', '察雅县', 3, '5403', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2953, '540326', '八宿县', 3, '5403', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2954, '540327', '左贡县', 3, '5403', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2955, '540328', '芒康县', 3, '5403', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2956, '540329', '洛隆县', 3, '5403', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2957, '540330', '边坝县', 3, '5403', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2958, '5404', '林芝市', 2, '54', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2959, '540402', '巴宜区', 3, '5404', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2960, '540421', '工布江达县', 3, '5404', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2961, '540423', '墨脱县', 3, '5404', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2962, '540424', '波密县', 3, '5404', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2963, '540425', '察隅县', 3, '5404', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2964, '540426', '朗县', 3, '5404', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2965, '540481', '米林市', 3, '5404', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2966, '5405', '山南市', 2, '54', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2967, '540502', '乃东区', 3, '5405', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2968, '540521', '扎囊县', 3, '5405', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2969, '540522', '贡嘎县', 3, '5405', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2970, '540523', '桑日县', 3, '5405', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2971, '540524', '琼结县', 3, '5405', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2972, '540525', '曲松县', 3, '5405', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2973, '540526', '措美县', 3, '5405', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2974, '540527', '洛扎县', 3, '5405', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2975, '540528', '加查县', 3, '5405', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2976, '540529', '隆子县', 3, '5405', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2977, '540531', '浪卡子县', 3, '5405', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2978, '540581', '错那市', 3, '5405', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2979, '5406', '那曲市', 2, '54', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2980, '540602', '色尼区', 3, '5406', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2981, '540621', '嘉黎县', 3, '5406', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2982, '540622', '比如县', 3, '5406', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2983, '540623', '聂荣县', 3, '5406', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2984, '540624', '安多县', 3, '5406', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2985, '540625', '申扎县', 3, '5406', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2986, '540626', '索县', 3, '5406', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2987, '540627', '班戈县', 3, '5406', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2988, '540628', '巴青县', 3, '5406', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2989, '540629', '尼玛县', 3, '5406', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2990, '540630', '双湖县', 3, '5406', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2991, '5425', '阿里地区', 2, '54', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2992, '542521', '普兰县', 3, '5425', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2993, '542522', '札达县', 3, '5425', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2994, '542523', '噶尔县', 3, '5425', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2995, '542524', '日土县', 3, '5425', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2996, '542525', '革吉县', 3, '5425', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2997, '542526', '改则县', 3, '5425', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2998, '542527', '措勤县', 3, '5425', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (2999, '61', '陕西省', 1, NULL, 27, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3000, '6101', '西安市', 2, '61', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3001, '610102', '新城区', 3, '6101', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3002, '610103', '碑林区', 3, '6101', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3003, '610104', '莲湖区', 3, '6101', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3004, '610111', '灞桥区', 3, '6101', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3005, '610112', '未央区', 3, '6101', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3006, '610113', '雁塔区', 3, '6101', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3007, '610114', '阎良区', 3, '6101', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3008, '610115', '临潼区', 3, '6101', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3009, '610116', '长安区', 3, '6101', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3010, '610117', '高陵区', 3, '6101', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3011, '610118', '鄠邑区', 3, '6101', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3012, '610122', '蓝田县', 3, '6101', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3013, '610124', '周至县', 3, '6101', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3014, '6102', '铜川市', 2, '61', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3015, '610202', '王益区', 3, '6102', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3016, '610203', '印台区', 3, '6102', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3017, '610204', '耀州区', 3, '6102', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3018, '610222', '宜君县', 3, '6102', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3019, '6103', '宝鸡市', 2, '61', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3020, '610302', '渭滨区', 3, '6103', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3021, '610303', '金台区', 3, '6103', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3022, '610304', '陈仓区', 3, '6103', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3023, '610305', '凤翔区', 3, '6103', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3024, '610323', '岐山县', 3, '6103', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3025, '610324', '扶风县', 3, '6103', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3026, '610326', '眉县', 3, '6103', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3027, '610327', '陇县', 3, '6103', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3028, '610328', '千阳县', 3, '6103', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3029, '610329', '麟游县', 3, '6103', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3030, '610330', '凤县', 3, '6103', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3031, '610331', '太白县', 3, '6103', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3032, '6104', '咸阳市', 2, '61', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3033, '610402', '秦都区', 3, '6104', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3034, '610403', '杨陵区', 3, '6104', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3035, '610404', '渭城区', 3, '6104', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3036, '610422', '三原县', 3, '6104', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3037, '610423', '泾阳县', 3, '6104', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3038, '610424', '乾县', 3, '6104', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3039, '610425', '礼泉县', 3, '6104', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3040, '610426', '永寿县', 3, '6104', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3041, '610428', '长武县', 3, '6104', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3042, '610429', '旬邑县', 3, '6104', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3043, '610430', '淳化县', 3, '6104', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3044, '610431', '武功县', 3, '6104', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3045, '610481', '兴平市', 3, '6104', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3046, '610482', '彬州市', 3, '6104', 14, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3047, '6105', '渭南市', 2, '61', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3048, '610502', '临渭区', 3, '6105', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3049, '610503', '华州区', 3, '6105', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3050, '610522', '潼关县', 3, '6105', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3051, '610523', '大荔县', 3, '6105', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3052, '610524', '合阳县', 3, '6105', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3053, '610525', '澄城县', 3, '6105', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3054, '610526', '蒲城县', 3, '6105', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3055, '610527', '白水县', 3, '6105', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3056, '610528', '富平县', 3, '6105', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3057, '610581', '韩城市', 3, '6105', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3058, '610582', '华阴市', 3, '6105', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3059, '6106', '延安市', 2, '61', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3060, '610602', '宝塔区', 3, '6106', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3061, '610603', '安塞区', 3, '6106', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3062, '610621', '延长县', 3, '6106', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3063, '610622', '延川县', 3, '6106', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3064, '610625', '志丹县', 3, '6106', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3065, '610626', '吴起县', 3, '6106', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3066, '610627', '甘泉县', 3, '6106', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3067, '610628', '富县', 3, '6106', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3068, '610629', '洛川县', 3, '6106', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3069, '610630', '宜川县', 3, '6106', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3070, '610631', '黄龙县', 3, '6106', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3071, '610632', '黄陵县', 3, '6106', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3072, '610681', '子长市', 3, '6106', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3073, '6107', '汉中市', 2, '61', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3074, '610702', '汉台区', 3, '6107', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3075, '610703', '南郑区', 3, '6107', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3076, '610722', '城固县', 3, '6107', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3077, '610723', '洋县', 3, '6107', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3078, '610724', '西乡县', 3, '6107', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3079, '610725', '勉县', 3, '6107', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3080, '610726', '宁强县', 3, '6107', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3081, '610727', '略阳县', 3, '6107', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3082, '610728', '镇巴县', 3, '6107', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3083, '610729', '留坝县', 3, '6107', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3084, '610730', '佛坪县', 3, '6107', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3085, '6108', '榆林市', 2, '61', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3086, '610802', '榆阳区', 3, '6108', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3087, '610803', '横山区', 3, '6108', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3088, '610822', '府谷县', 3, '6108', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3089, '610824', '靖边县', 3, '6108', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3090, '610825', '定边县', 3, '6108', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3091, '610826', '绥德县', 3, '6108', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3092, '610827', '米脂县', 3, '6108', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3093, '610828', '佳县', 3, '6108', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3094, '610829', '吴堡县', 3, '6108', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3095, '610830', '清涧县', 3, '6108', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3096, '610831', '子洲县', 3, '6108', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3097, '610881', '神木市', 3, '6108', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3098, '6109', '安康市', 2, '61', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3099, '610902', '汉滨区', 3, '6109', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3100, '610921', '汉阴县', 3, '6109', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3101, '610922', '石泉县', 3, '6109', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3102, '610923', '宁陕县', 3, '6109', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3103, '610924', '紫阳县', 3, '6109', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3104, '610925', '岚皋县', 3, '6109', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3105, '610926', '平利县', 3, '6109', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3106, '610927', '镇坪县', 3, '6109', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3107, '610929', '白河县', 3, '6109', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3108, '610981', '旬阳市', 3, '6109', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3109, '6110', '商洛市', 2, '61', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3110, '611002', '商州区', 3, '6110', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3111, '611021', '洛南县', 3, '6110', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3112, '611022', '丹凤县', 3, '6110', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3113, '611023', '商南县', 3, '6110', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3114, '611024', '山阳县', 3, '6110', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3115, '611025', '镇安县', 3, '6110', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3116, '611026', '柞水县', 3, '6110', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3117, '62', '甘肃省', 1, NULL, 28, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3118, '6201', '兰州市', 2, '62', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3119, '620102', '城关区', 3, '6201', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3120, '620103', '七里河区', 3, '6201', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3121, '620104', '西固区', 3, '6201', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3122, '620105', '安宁区', 3, '6201', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3123, '620111', '红古区', 3, '6201', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3124, '620121', '永登县', 3, '6201', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3125, '620122', '皋兰县', 3, '6201', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3126, '620123', '榆中县', 3, '6201', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3127, '620171', '兰州新区', 3, '6201', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3128, '6202', '嘉峪关市', 2, '62', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3129, '620201001', '雄关街道', 3, '6202', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3130, '620201002', '钢城街道', 3, '6202', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3131, '620201100', '新城镇', 3, '6202', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3132, '620201101', '峪泉镇', 3, '6202', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3133, '620201102', '文殊镇', 3, '6202', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3134, '6203', '金昌市', 2, '62', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3135, '620302', '金川区', 3, '6203', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3136, '620321', '永昌县', 3, '6203', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3137, '6204', '白银市', 2, '62', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3138, '620402', '白银区', 3, '6204', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3139, '620403', '平川区', 3, '6204', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3140, '620421', '靖远县', 3, '6204', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3141, '620422', '会宁县', 3, '6204', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3142, '620423', '景泰县', 3, '6204', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3143, '6205', '天水市', 2, '62', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3144, '620502', '秦州区', 3, '6205', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3145, '620503', '麦积区', 3, '6205', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3146, '620521', '清水县', 3, '6205', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3147, '620522', '秦安县', 3, '6205', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3148, '620523', '甘谷县', 3, '6205', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3149, '620524', '武山县', 3, '6205', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3150, '620525', '张家川回族自治县', 3, '6205', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3151, '6206', '武威市', 2, '62', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3152, '620602', '凉州区', 3, '6206', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3153, '620621', '民勤县', 3, '6206', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3154, '620622', '古浪县', 3, '6206', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3155, '620623', '天祝藏族自治县', 3, '6206', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3156, '6207', '张掖市', 2, '62', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3157, '620702', '甘州区', 3, '6207', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3158, '620721', '肃南裕固族自治县', 3, '6207', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3159, '620722', '民乐县', 3, '6207', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3160, '620723', '临泽县', 3, '6207', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3161, '620724', '高台县', 3, '6207', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3162, '620725', '山丹县', 3, '6207', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3163, '6208', '平凉市', 2, '62', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3164, '620802', '崆峒区', 3, '6208', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3165, '620821', '泾川县', 3, '6208', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3166, '620822', '灵台县', 3, '6208', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3167, '620823', '崇信县', 3, '6208', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3168, '620825', '庄浪县', 3, '6208', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3169, '620826', '静宁县', 3, '6208', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3170, '620881', '华亭市', 3, '6208', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3171, '6209', '酒泉市', 2, '62', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3172, '620902', '肃州区', 3, '6209', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3173, '620921', '金塔县', 3, '6209', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3174, '620922', '瓜州县', 3, '6209', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3175, '620923', '肃北蒙古族自治县', 3, '6209', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3176, '620924', '阿克塞哈萨克族自治县', 3, '6209', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3177, '620981', '玉门市', 3, '6209', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3178, '620982', '敦煌市', 3, '6209', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3179, '6210', '庆阳市', 2, '62', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3180, '621002', '西峰区', 3, '6210', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3181, '621021', '庆城县', 3, '6210', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3182, '621022', '环县', 3, '6210', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3183, '621023', '华池县', 3, '6210', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3184, '621024', '合水县', 3, '6210', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3185, '621025', '正宁县', 3, '6210', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3186, '621026', '宁县', 3, '6210', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3187, '621027', '镇原县', 3, '6210', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3188, '6211', '定西市', 2, '62', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3189, '621102', '安定区', 3, '6211', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3190, '621121', '通渭县', 3, '6211', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3191, '621122', '陇西县', 3, '6211', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3192, '621123', '渭源县', 3, '6211', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3193, '621124', '临洮县', 3, '6211', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3194, '621125', '漳县', 3, '6211', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3195, '621126', '岷县', 3, '6211', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3196, '6212', '陇南市', 2, '62', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3197, '621202', '武都区', 3, '6212', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3198, '621221', '成县', 3, '6212', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3199, '621222', '文县', 3, '6212', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3200, '621223', '宕昌县', 3, '6212', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3201, '621224', '康县', 3, '6212', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3202, '621225', '西和县', 3, '6212', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3203, '621226', '礼县', 3, '6212', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3204, '621227', '徽县', 3, '6212', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3205, '621228', '两当县', 3, '6212', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3206, '6229', '临夏回族自治州', 2, '62', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3207, '622901', '临夏市', 3, '6229', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3208, '622921', '临夏县', 3, '6229', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3209, '622922', '康乐县', 3, '6229', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3210, '622923', '永靖县', 3, '6229', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3211, '622924', '广河县', 3, '6229', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3212, '622925', '和政县', 3, '6229', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3213, '622926', '东乡族自治县', 3, '6229', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3214, '622927', '积石山保安族东乡族撒拉族自治县', 3, '6229', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3215, '6230', '甘南藏族自治州', 2, '62', 14, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3216, '623001', '合作市', 3, '6230', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3217, '623021', '临潭县', 3, '6230', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3218, '623022', '卓尼县', 3, '6230', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3219, '623023', '舟曲县', 3, '6230', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3220, '623024', '迭部县', 3, '6230', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3221, '623025', '玛曲县', 3, '6230', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3222, '623026', '碌曲县', 3, '6230', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3223, '623027', '夏河县', 3, '6230', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3224, '63', '青海省', 1, NULL, 29, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3225, '6301', '西宁市', 2, '63', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3226, '630102', '城东区', 3, '6301', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3227, '630103', '城中区', 3, '6301', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3228, '630104', '城西区', 3, '6301', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3229, '630105', '城北区', 3, '6301', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3230, '630106', '湟中区', 3, '6301', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3231, '630121', '大通回族土族自治县', 3, '6301', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3232, '630123', '湟源县', 3, '6301', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3233, '6302', '海东市', 2, '63', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3234, '630202', '乐都区', 3, '6302', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3235, '630203', '平安区', 3, '6302', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3236, '630222', '民和回族土族自治县', 3, '6302', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3237, '630223', '互助土族自治县', 3, '6302', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3238, '630224', '化隆回族自治县', 3, '6302', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3239, '630225', '循化撒拉族自治县', 3, '6302', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3240, '6322', '海北藏族自治州', 2, '63', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3241, '632221', '门源回族自治县', 3, '6322', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3242, '632222', '祁连县', 3, '6322', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3243, '632223', '海晏县', 3, '6322', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3244, '632224', '刚察县', 3, '6322', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3245, '6323', '黄南藏族自治州', 2, '63', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3246, '632301', '同仁市', 3, '6323', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3247, '632322', '尖扎县', 3, '6323', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3248, '632323', '泽库县', 3, '6323', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3249, '632324', '河南蒙古族自治县', 3, '6323', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3250, '6325', '海南藏族自治州', 2, '63', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3251, '632521', '共和县', 3, '6325', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3252, '632522', '同德县', 3, '6325', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3253, '632523', '贵德县', 3, '6325', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3254, '632524', '兴海县', 3, '6325', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3255, '632525', '贵南县', 3, '6325', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3256, '6326', '果洛藏族自治州', 2, '63', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3257, '632621', '玛沁县', 3, '6326', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3258, '632622', '班玛县', 3, '6326', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3259, '632623', '甘德县', 3, '6326', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3260, '632624', '达日县', 3, '6326', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3261, '632625', '久治县', 3, '6326', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3262, '632626', '玛多县', 3, '6326', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3263, '6327', '玉树藏族自治州', 2, '63', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3264, '632701', '玉树市', 3, '6327', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3265, '632722', '杂多县', 3, '6327', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3266, '632723', '称多县', 3, '6327', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3267, '632724', '治多县', 3, '6327', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3268, '632725', '囊谦县', 3, '6327', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3269, '632726', '曲麻莱县', 3, '6327', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3270, '6328', '海西蒙古族藏族自治州', 2, '63', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3271, '632801', '格尔木市', 3, '6328', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3272, '632802', '德令哈市', 3, '6328', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3273, '632803', '茫崖市', 3, '6328', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3274, '632821', '乌兰县', 3, '6328', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3275, '632822', '都兰县', 3, '6328', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3276, '632823', '天峻县', 3, '6328', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3277, '632857', '大柴旦行政委员会', 3, '6328', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3278, '64', '宁夏回族自治区', 1, NULL, 30, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3279, '6401', '银川市', 2, '64', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3280, '640104', '兴庆区', 3, '6401', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3281, '640105', '西夏区', 3, '6401', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3282, '640106', '金凤区', 3, '6401', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3283, '640121', '永宁县', 3, '6401', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3284, '640122', '贺兰县', 3, '6401', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3285, '640181', '灵武市', 3, '6401', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3286, '6402', '石嘴山市', 2, '64', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3287, '640202', '大武口区', 3, '6402', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3288, '640205', '惠农区', 3, '6402', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3289, '640221', '平罗县', 3, '6402', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3290, '6403', '吴忠市', 2, '64', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3291, '640302', '利通区', 3, '6403', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3292, '640303', '红寺堡区', 3, '6403', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3293, '640323', '盐池县', 3, '6403', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3294, '640324', '同心县', 3, '6403', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3295, '640381', '青铜峡市', 3, '6403', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3296, '6404', '固原市', 2, '64', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3297, '640402', '原州区', 3, '6404', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3298, '640422', '西吉县', 3, '6404', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3299, '640423', '隆德县', 3, '6404', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3300, '640424', '泾源县', 3, '6404', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3301, '640425', '彭阳县', 3, '6404', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3302, '6405', '中卫市', 2, '64', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3303, '640502', '沙坡头区', 3, '6405', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3304, '640521', '中宁县', 3, '6405', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3305, '640522', '海原县', 3, '6405', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3306, '65', '新疆维吾尔自治区', 1, NULL, 31, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3307, '6501', '乌鲁木齐市', 2, '65', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3308, '650102', '天山区', 3, '6501', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3309, '650103', '沙依巴克区', 3, '6501', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3310, '650104', '新市区', 3, '6501', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3311, '650105', '水磨沟区', 3, '6501', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3312, '650106', '头屯河区', 3, '6501', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3313, '650107', '达坂城区', 3, '6501', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3314, '650109', '米东区', 3, '6501', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3315, '650121', '乌鲁木齐县', 3, '6501', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3316, '6502', '克拉玛依市', 2, '65', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3317, '650202', '独山子区', 3, '6502', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3318, '650203', '克拉玛依区', 3, '6502', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3319, '650204', '白碱滩区', 3, '6502', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3320, '650205', '乌尔禾区', 3, '6502', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3321, '6504', '吐鲁番市', 2, '65', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3322, '650402', '高昌区', 3, '6504', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3323, '650421', '鄯善县', 3, '6504', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3324, '650422', '托克逊县', 3, '6504', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3325, '6505', '哈密市', 2, '65', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3326, '650502', '伊州区', 3, '6505', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3327, '650521', '巴里坤哈萨克自治县', 3, '6505', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3328, '650522', '伊吾县', 3, '6505', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3329, '6523', '昌吉回族自治州', 2, '65', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3330, '652301', '昌吉市', 3, '6523', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3331, '652302', '阜康市', 3, '6523', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3332, '652323', '呼图壁县', 3, '6523', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3333, '652324', '玛纳斯县', 3, '6523', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3334, '652325', '奇台县', 3, '6523', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3335, '652327', '吉木萨尔县', 3, '6523', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3336, '652328', '木垒哈萨克自治县', 3, '6523', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3337, '6527', '博尔塔拉蒙古自治州', 2, '65', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3338, '652701', '博乐市', 3, '6527', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3339, '652702', '阿拉山口市', 3, '6527', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3340, '652722', '精河县', 3, '6527', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3341, '652723', '温泉县', 3, '6527', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3342, '6528', '巴音郭楞蒙古自治州', 2, '65', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3343, '652801', '库尔勒市', 3, '6528', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3344, '652822', '轮台县', 3, '6528', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3345, '652823', '尉犁县', 3, '6528', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3346, '652824', '若羌县', 3, '6528', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3347, '652825', '且末县', 3, '6528', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3348, '652826', '焉耆回族自治县', 3, '6528', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3349, '652827', '和静县', 3, '6528', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3350, '652828', '和硕县', 3, '6528', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3351, '652829', '博湖县', 3, '6528', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3352, '6529', '阿克苏地区', 2, '65', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3353, '652901', '阿克苏市', 3, '6529', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3354, '652902', '库车市', 3, '6529', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3355, '652922', '温宿县', 3, '6529', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3356, '652924', '沙雅县', 3, '6529', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3357, '652925', '新和县', 3, '6529', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3358, '652926', '拜城县', 3, '6529', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3359, '652927', '乌什县', 3, '6529', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3360, '652928', '阿瓦提县', 3, '6529', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3361, '652929', '柯坪县', 3, '6529', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3362, '6530', '克孜勒苏柯尔克孜自治州', 2, '65', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3363, '653001', '阿图什市', 3, '6530', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3364, '653022', '阿克陶县', 3, '6530', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3365, '653023', '阿合奇县', 3, '6530', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3366, '653024', '乌恰县', 3, '6530', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3367, '6531', '喀什地区', 2, '65', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3368, '653101', '喀什市', 3, '6531', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3369, '653121', '疏附县', 3, '6531', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3370, '653122', '疏勒县', 3, '6531', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3371, '653123', '英吉沙县', 3, '6531', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3372, '653124', '泽普县', 3, '6531', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3373, '653125', '莎车县', 3, '6531', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3374, '653126', '叶城县', 3, '6531', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3375, '653127', '麦盖提县', 3, '6531', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3376, '653128', '岳普湖县', 3, '6531', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3377, '653129', '伽师县', 3, '6531', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3378, '653130', '巴楚县', 3, '6531', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3379, '653131', '塔什库尔干塔吉克自治县', 3, '6531', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3380, '6532', '和田地区', 2, '65', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3381, '653201', '和田市', 3, '6532', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3382, '653221', '和田县', 3, '6532', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3383, '653222', '墨玉县', 3, '6532', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3384, '653223', '皮山县', 3, '6532', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3385, '653224', '洛浦县', 3, '6532', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3386, '653225', '策勒县', 3, '6532', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3387, '653226', '于田县', 3, '6532', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3388, '653227', '民丰县', 3, '6532', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3389, '6540', '伊犁哈萨克自治州', 2, '65', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3390, '654002', '伊宁市', 3, '6540', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3391, '654003', '奎屯市', 3, '6540', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3392, '654004', '霍尔果斯市', 3, '6540', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3393, '654021', '伊宁县', 3, '6540', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3394, '654022', '察布查尔锡伯自治县', 3, '6540', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3395, '654023', '霍城县', 3, '6540', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3396, '654024', '巩留县', 3, '6540', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3397, '654025', '新源县', 3, '6540', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3398, '654026', '昭苏县', 3, '6540', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3399, '654027', '特克斯县', 3, '6540', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3400, '654028', '尼勒克县', 3, '6540', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3401, '6542', '塔城地区', 2, '65', 13, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3402, '654201', '塔城市', 3, '6542', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3403, '654202', '乌苏市', 3, '6542', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3404, '654203', '沙湾市', 3, '6542', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3405, '654221', '额敏县', 3, '6542', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3406, '654224', '托里县', 3, '6542', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3407, '654225', '裕民县', 3, '6542', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3408, '654226', '和布克赛尔蒙古自治县', 3, '6542', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3409, '6543', '阿勒泰地区', 2, '65', 14, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3410, '654301', '阿勒泰市', 3, '6543', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3411, '654321', '布尔津县', 3, '6543', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3412, '654322', '富蕴县', 3, '6543', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3413, '654323', '福海县', 3, '6543', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3414, '654324', '哈巴河县', 3, '6543', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3415, '654325', '青河县', 3, '6543', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3416, '654326', '吉木乃县', 3, '6543', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3417, '6590', '自治区直辖县级行政区划', 2, '65', 15, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3418, '659001', '石河子市', 3, '6590', 1, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3419, '659002', '阿拉尔市', 3, '6590', 2, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3420, '659003', '图木舒克市', 3, '6590', 3, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3421, '659004', '五家渠市', 3, '6590', 4, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3422, '659005', '北屯市', 3, '6590', 5, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3423, '659006', '铁门关市', 3, '6590', 6, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3424, '659007', '双河市', 3, '6590', 7, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3425, '659008', '可克达拉市', 3, '6590', 8, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3426, '659009', '昆玉市', 3, '6590', 9, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3427, '659010', '胡杨河市', 3, '6590', 10, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3428, '659011', '新星市', 3, '6590', 11, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');
INSERT INTO `china_region` VALUES (3429, '659012', '白杨市', 3, '6590', 12, 1, '2025-10-02 02:34:59', '2025-10-02 02:34:59');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `user_id` int NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` int NULL DEFAULT 0,
  `likes_count` int NULL DEFAULT 0,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 1, 3, 'Xiaobai is so cute!', 0, 1, 1, '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `comment` VALUES (2, 1, 1, 'Golden Retriever is very gentle', 0, 0, 1, '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `comment` VALUES (3, 2, 2, 'Diet control is important', 0, 0, 1, '2025-09-30 23:35:45', '2025-09-30 23:35:45');

-- ----------------------------
-- Table structure for community_statistics
-- ----------------------------
DROP TABLE IF EXISTS `community_statistics`;
CREATE TABLE `community_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `stat_date` date NOT NULL,
  `post_count` int NULL DEFAULT 0,
  `comment_count` int NULL DEFAULT 0,
  `user_count` int NULL DEFAULT 0,
  `active_user_count` int NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of community_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `discount_type` int NULL DEFAULT 1,
  `discount_value` decimal(10, 2) NULL DEFAULT NULL,
  `min_amount` decimal(10, 2) NULL DEFAULT 0.00,
  `expire_date` datetime NULL DEFAULT NULL,
  `total_count` int NULL DEFAULT 0,
  `used_count` int NULL DEFAULT 0,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupon
-- ----------------------------
INSERT INTO `coupon` VALUES (1, 'New User Coupon', 'New user special', 1, 10.00, 100.00, NULL, 100, 0, 1, '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `coupon` VALUES (2, 'Discount Coupon', '200 off 20', 1, 20.00, 200.00, NULL, 50, 0, 1, '2025-09-30 23:35:45', '2025-09-30 23:35:45');

-- ----------------------------
-- Table structure for data_security_log
-- ----------------------------
DROP TABLE IF EXISTS `data_security_log`;
CREATE TABLE `data_security_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `result` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_action`(`action` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of data_security_log
-- ----------------------------

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `specialization` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `rating` decimal(3, 1) NULL DEFAULT 0.0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'online' COMMENT 'online/offline',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor
-- ----------------------------
INSERT INTO `doctor` VALUES (9, '张伟医师', '/upload/doctor1.png', '犬猫综合科', '主治医师', '从业10年，擅长犬猫内科疾病诊断与治疗，对传染病、消化系统疾病、呼吸系统疾病有丰富临床经验，深受客户信赖', 4.8, 'online', '2025-10-09 20:16:26', '2025-10-09 20:16:26');
INSERT INTO `doctor` VALUES (10, '李娜医师', '/upload/doctor2.png', '猫科专科', '副主任医师', '猫科疾病专家，从业12年，擅长猫咪泌尿系统疾病、皮肤病及猫传染性腹膜炎等疑难病症，对猫行为学也有深入研究', 4.9, 'online', '2025-10-09 20:16:26', '2025-10-09 20:16:26');
INSERT INTO `doctor` VALUES (11, '王伟医师', '/upload/doctor3.png', '外科手术', '主治医师', '外科手术专家，从业15年，擅长骨科手术、软组织修复、微创手术等，累计成功手术3000余例，手术成功率达98%', 4.7, 'online', '2025-10-09 20:16:26', '2025-10-09 20:16:26');
INSERT INTO `doctor` VALUES (12, '刘红医师', '/upload/doctor4.png', '异宠专科', '主治医师', '异宠医疗专家，从业8年，擅长兔子、仓鼠、鸟类等小型宠物的诊疗，对异宠营养学、中毒救治有丰富经验', 4.6, 'online', '2025-10-09 20:16:26', '2025-10-09 20:16:26');
INSERT INTO `doctor` VALUES (13, '陈静医师', '/upload/doctor5.png', '皮肤病专科', '副主任医师', '宠物皮肤病专家，从业11年，擅长犬猫皮肤病、过敏性疾病、真菌感染、细菌感染等疾病的诊断和治疗，对皮肤病有丰富经验', 4.8, 'online', '2025-10-09 20:16:26', '2025-10-09 20:16:26');

-- ----------------------------
-- Table structure for favorite_record
-- ----------------------------
DROP TABLE IF EXISTS `favorite_record`;
CREATE TABLE `favorite_record`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of favorite_record
-- ----------------------------

-- ----------------------------
-- Table structure for featured_post
-- ----------------------------
DROP TABLE IF EXISTS `featured_post`;
CREATE TABLE `featured_post`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `featured_order` int NULL DEFAULT 0,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_featured_order`(`featured_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of featured_post
-- ----------------------------

-- ----------------------------
-- Table structure for grooming_service_banners
-- ----------------------------
DROP TABLE IF EXISTS `grooming_service_banners`;
CREATE TABLE `grooming_service_banners`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active',
  `sort_order` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of grooming_service_banners
-- ----------------------------
INSERT INTO `grooming_service_banners` VALUES (1, '1', '1', '/upload/default-grooming-banner.jpg', 'grooming-page-top', 'active', 1, '2025-10-11 17:48:02', '2025-10-11 23:10:12', 0);

-- ----------------------------
-- Table structure for grooming_services
-- ----------------------------
DROP TABLE IF EXISTS `grooming_services`;
CREATE TABLE `grooming_services`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bg_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#e3f2fd',
  `duration` int NOT NULL DEFAULT 60,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active',
  `sort_order` int NULL DEFAULT 0,
  `is_recommended` tinyint(1) NULL DEFAULT 0,
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_is_recommended`(`is_recommended` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of grooming_services
-- ----------------------------

-- ----------------------------
-- Table structure for like_record
-- ----------------------------
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_id` int NOT NULL,
  `post_id` int NULL DEFAULT NULL COMMENT '???ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of like_record
-- ----------------------------

-- ----------------------------
-- Table structure for litter_service
-- ----------------------------
DROP TABLE IF EXISTS `litter_service`;
CREATE TABLE `litter_service`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `staff_id` bigint NULL DEFAULT NULL,
  `pet_id` bigint NULL DEFAULT NULL,
  `service_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `detail_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `service_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `service_time` datetime NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending',
  `price` decimal(10, 2) NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `special_requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `completed_time` datetime NULL DEFAULT NULL,
  `rating` int NULL DEFAULT NULL,
  `review` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `staff_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏈嶅姟浜哄憳濮撳悕',
  `staff_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏈嶅姟浜哄憳鐢佃瘽',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of litter_service
-- ----------------------------

-- ----------------------------
-- Table structure for location_service
-- ----------------------------
DROP TABLE IF EXISTS `location_service`;
CREATE TABLE `location_service`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `service_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `service_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `latitude` decimal(10, 8) NULL DEFAULT NULL,
  `longitude` decimal(11, 8) NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `rating` decimal(3, 1) NULL DEFAULT NULL,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_service_type`(`service_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of location_service
-- ----------------------------

-- ----------------------------
-- Table structure for medical_services
-- ----------------------------
DROP TABLE IF EXISTS `medical_services`;
CREATE TABLE `medical_services`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '服务ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '服务描述',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '服务价格',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '医疗服务' COMMENT '服务分类',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务图片URL',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '服务状态：active-启用，inactive-禁用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `product_introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '鍟嗗搧浠嬬粛',
  `usage_instructions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '浣跨敤椤荤煡',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '医疗服务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of medical_services
-- ----------------------------
INSERT INTO `medical_services` VALUES (2, '疫苗接种服务', '专业疫苗接种，预防各种疾病，包括狂犬病、猫三联、狗六联等', 120.00, '医疗服务', '/upload/1759914861507_2.jpg', 'active', 2, '2025-10-05 20:04:45', '2025-10-08 17:14:22', 1, 7, '11', NULL);
INSERT INTO `medical_services` VALUES (3, '宠物手术服务', '专业手术治疗，包括绝育手术、肿瘤切除、骨折修复等外科手术', 800.00, '医疗服务', '/upload/1759917893453_3.jpg', 'active', 3, '2025-10-05 20:04:45', '2025-10-08 18:04:53', 1, 7, NULL, NULL);
INSERT INTO `medical_services` VALUES (4, '急诊医疗服务', '24小时急诊服务，快速响应紧急情况，为宠物提供及时的医疗救治', 300.00, '医疗服务', '/upload/1759927540309_4.jpg', 'active', 4, '2025-10-05 20:04:45', '2025-10-08 20:45:40', 1, 7, NULL, NULL);
INSERT INTO `medical_services` VALUES (5, '宠物美容服务', '专业宠物美容，包括洗澡、修剪、造型等服务', 150.00, '美容服务', '/upload/1759927161791_5.jpg', 'active', 5, '2025-10-05 20:04:45', '2025-10-08 20:39:22', 1, 7, NULL, NULL);
INSERT INTO `medical_services` VALUES (6, '宠物寄养服务', '安全舒适的寄养环境，24小时专人看护，让主人安心出行', 80.00, '寄养服务', '/static/default-pet.png', 'active', 6, '2025-10-05 20:04:45', '2025-10-08 03:29:34', 1, 7, NULL, NULL);

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 0,
  `read_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notification
-- ----------------------------

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 1, 1, 1, 89.00, '2025-09-30 23:35:45');
INSERT INTO `order_item` VALUES (2, 1, 3, 1, 25.00, '2025-09-30 23:35:45');
INSERT INTO `order_item` VALUES (3, 2, 1, 1, 89.00, '2025-09-30 23:35:45');

-- ----------------------------
-- Table structure for payment
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `amount` decimal(10, 2) NOT NULL,
  `status` int NULL DEFAULT 0,
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment
-- ----------------------------
INSERT INTO `payment` VALUES (1, 1, 'WeChat Pay', 114.00, 1, 'wx202509300001', '2025-09-30 23:35:45', '2025-09-30 23:35:45');

-- ----------------------------
-- Table structure for pet
-- ----------------------------
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `species` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `breed` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `age` int NULL DEFAULT NULL,
  `weight` decimal(5, 2) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active',
  `birthday` date NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet
-- ----------------------------
INSERT INTO `pet` VALUES (1, 5, 'pet1', 'dog', 'golden', 'male', 2, NULL, 'active', NULL, NULL, NULL, '/static/default-pet.png', '2025-10-08 03:40:17', '2025-10-08 03:40:17');
INSERT INTO `pet` VALUES (2, 5, 'pet2', 'cat', 'shorthair', 'female', 1, NULL, 'active', NULL, NULL, NULL, '/static/default-pet.png', '2025-10-08 03:40:17', '2025-10-08 03:40:17');

-- ----------------------------
-- Table structure for pet_boarding
-- ----------------------------
DROP TABLE IF EXISTS `pet_boarding`;
CREATE TABLE `pet_boarding`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `pet_id` bigint NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `boarding_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending',
  `price` decimal(10, 2) NOT NULL,
  `special_requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `pet_health_status` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `vaccination_records` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `emergency_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `actual_start_time` datetime NULL DEFAULT NULL,
  `actual_end_time` datetime NULL DEFAULT NULL,
  `rating` int NULL DEFAULT NULL,
  `review` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pet_boarding
-- ----------------------------
INSERT INTO `pet_boarding` VALUES (1, 5, 1, '2025-10-13 09:00:00', '2025-10-13 18:00:00', 'monthly', 'cancelled', 2500.00, '1', '1', '1', '1', '1', '用户提交的寄养申请', NULL, NULL, NULL, NULL, '2025-10-11 00:21:48', '2025-10-11 13:58:02');
INSERT INTO `pet_boarding` VALUES (2, 5, 1, '2025-10-13 09:00:00', '2025-10-13 18:00:00', 'monthly', 'cancelled', 2500.00, '1', '1', '1', '1', '1', '用户提交的寄养申请', NULL, NULL, NULL, NULL, '2025-10-11 00:22:06', '2025-10-11 13:58:42');
INSERT INTO `pet_boarding` VALUES (3, 5, 1, '2025-10-12 09:00:00', '2025-10-13 18:00:00', 'monthly', 'cancelled', 2500.00, '1', '1', '1', '1', '1', '用户提交的寄养申请', NULL, NULL, NULL, NULL, '2025-10-11 12:40:09', '2025-10-11 13:58:52');
INSERT INTO `pet_boarding` VALUES (4, 5, 1, '2025-10-14 09:00:00', '2025-10-15 18:00:00', 'monthly', 'cancelled', 2500.00, '1', '1', '1', '12', '12', '用户提交的寄养申请', NULL, NULL, NULL, NULL, '2025-10-11 13:05:49', '2025-10-11 13:58:40');
INSERT INTO `pet_boarding` VALUES (5, 5, 1, '2025-10-12 09:00:00', '2025-10-13 18:00:00', 'monthly', 'in_progress', 2500.00, '1', '1', '1', '1', '1', '用户提交的寄养申请', NULL, NULL, NULL, NULL, '2025-10-11 14:03:09', '2025-10-11 14:13:04');

-- ----------------------------
-- Table structure for pet_growth
-- ----------------------------
DROP TABLE IF EXISTS `pet_growth`;
CREATE TABLE `pet_growth`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `pet_id` int NOT NULL,
  `age_months` int NULL DEFAULT NULL,
  `weight` decimal(5, 2) NULL DEFAULT NULL,
  `height` decimal(5, 2) NULL DEFAULT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `record_date` date NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_growth
-- ----------------------------
INSERT INTO `pet_growth` VALUES (1, 1, 36, 25.50, 60.00, 'Healthy growth', '2025-09-30', '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `pet_growth` VALUES (2, 2, 24, 4.20, 25.00, 'Normal development', '2025-09-30', '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `pet_growth` VALUES (3, 5, NULL, 1.00, 1.00, NULL, '2025-10-01', '2025-10-01 14:05:18', '2025-10-01 14:05:18');
INSERT INTO `pet_growth` VALUES (4, 5, NULL, 33.00, 2.00, NULL, '2025-10-01', '2025-10-01 14:19:27', '2025-10-01 14:19:27');
INSERT INTO `pet_growth` VALUES (5, 5, NULL, 2.00, 2.00, NULL, '2025-10-01', '2025-10-01 14:19:50', '2025-10-01 14:19:50');
INSERT INTO `pet_growth` VALUES (6, 1, NULL, 26.00, 62.00, NULL, '2025-10-01', '2025-10-01 14:21:14', '2025-10-01 14:21:14');
INSERT INTO `pet_growth` VALUES (7, 1, NULL, 26.50, 63.00, NULL, '2025-10-02', '2025-10-01 14:21:27', '2025-10-01 14:21:27');

-- ----------------------------
-- Table structure for pet_health
-- ----------------------------
DROP TABLE IF EXISTS `pet_health`;
CREATE TABLE `pet_health`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `pet_id` int NOT NULL,
  `health_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `weight` decimal(5, 2) NULL DEFAULT NULL,
  `temperature` decimal(4, 1) NULL DEFAULT NULL,
  `symptoms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `diagnosis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `treatment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `vet_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `record_date` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_health
-- ----------------------------
INSERT INTO `pet_health` VALUES (1, 1, 'Healthy', 25.50, 38.5, 'None', 'Healthy', 'Keep maintaining', 'Dr. Li', '2025-09-30 00:00:00', '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `pet_health` VALUES (2, 2, 'Good', 4.20, 38.8, 'None', 'Good', 'Regular check', 'Dr. Wang', '2025-09-30 00:00:00', '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `pet_health` VALUES (3, 5, '良好', 1.00, 32.0, '1', '1', '1', '1', NULL, '2025-10-01 14:10:04', '2025-10-01 14:10:04');

-- ----------------------------
-- Table structure for pet_health_statistics
-- ----------------------------
DROP TABLE IF EXISTS `pet_health_statistics`;
CREATE TABLE `pet_health_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `pet_id` int NOT NULL,
  `stat_date` date NOT NULL,
  `health_score` decimal(3, 1) NULL DEFAULT NULL,
  `weight` decimal(5, 2) NULL DEFAULT NULL,
  `temperature` decimal(4, 1) NULL DEFAULT NULL,
  `exercise_time` int NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pet_date`(`pet_id` ASC, `stat_date` ASC) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_health_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for pet_order
-- ----------------------------
DROP TABLE IF EXISTS `pet_order`;
CREATE TABLE `pet_order`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL,
  `status` int NULL DEFAULT 0,
  `payment_status` int NULL DEFAULT 0,
  `delivery_status` int NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_order
-- ----------------------------
INSERT INTO `pet_order` VALUES (1, 2, 'PO202509300001', 114.00, 1, 1, 1, '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `pet_order` VALUES (2, 3, 'PO202509300002', 89.00, 0, 0, 0, '2025-09-30 23:35:45', '2025-09-30 23:35:45');

-- ----------------------------
-- Table structure for pet_social
-- ----------------------------
DROP TABLE IF EXISTS `pet_social`;
CREATE TABLE `pet_social`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `pet_id` int NOT NULL,
  `user_id` int NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `likes_count` int NULL DEFAULT 0,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_social
-- ----------------------------
INSERT INTO `pet_social` VALUES (1, 1, 3, 'test content', '', 0, 1, '2025-10-01 15:09:59', '2025-10-01 15:09:59');
INSERT INTO `pet_social` VALUES (2, 1, 2, '测试动态', NULL, 0, 1, '2025-10-01 22:19:03', '2025-10-01 22:19:03');
INSERT INTO `pet_social` VALUES (3, 5, 5, '1', 'http://tmp/SHkMKNXzNswHde2fea74dc0441c47a7a9047cbc8e884.jpg', 0, 1, '2025-10-01 22:26:09', '2025-10-01 22:26:09');

-- ----------------------------
-- Table structure for pet_social_like
-- ----------------------------
DROP TABLE IF EXISTS `pet_social_like`;
CREATE TABLE `pet_social_like`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `social_id` int NOT NULL,
  `user_id` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_social_user`(`social_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_social_id`(`social_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pet_social_like
-- ----------------------------

-- ----------------------------
-- Table structure for points_record
-- ----------------------------
DROP TABLE IF EXISTS `points_record`;
CREATE TABLE `points_record`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `points` int NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of points_record
-- ----------------------------
INSERT INTO `points_record` VALUES (1, 5, 10, 'earn', '每日签到奖励', '2025-10-03 15:04:56');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '甯栧瓙鍒嗙被',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `likes_count` int NULL DEFAULT 0,
  `comments_count` int NULL DEFAULT 0,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post
-- ----------------------------

-- ----------------------------
-- Table structure for post_favorite
-- ----------------------------
DROP TABLE IF EXISTS `post_favorite`;
CREATE TABLE `post_favorite`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `user_id` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_favorite
-- ----------------------------

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `user_id` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_like
-- ----------------------------

-- ----------------------------
-- Table structure for post_report
-- ----------------------------
DROP TABLE IF EXISTS `post_report`;
CREATE TABLE `post_report`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `user_id` int NOT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `status` int NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_report
-- ----------------------------

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `price` decimal(10, 2) NOT NULL,
  `stock` int NULL DEFAULT 0,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_hot` tinyint(1) NULL DEFAULT 0 COMMENT 'is hot recommendation',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 121 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product
-- ----------------------------

-- ----------------------------
-- Table structure for product_favorite
-- ----------------------------
DROP TABLE IF EXISTS `product_favorite`;
CREATE TABLE `product_favorite`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_favorite
-- ----------------------------
INSERT INTO `product_favorite` VALUES (37, 5, 1, '2025-10-03 03:12:10', '2025-10-03 03:12:10');
INSERT INTO `product_favorite` VALUES (38, 5, 2, '2025-10-03 03:12:12', '2025-10-03 03:12:12');

-- ----------------------------
-- Table structure for product_recommendation
-- ----------------------------
DROP TABLE IF EXISTS `product_recommendation`;
CREATE TABLE `product_recommendation`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `score` decimal(5, 2) NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_recommendation
-- ----------------------------

-- ----------------------------
-- Table structure for product_review
-- ----------------------------
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `user_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_review
-- ----------------------------

-- ----------------------------
-- Table structure for product_statistics
-- ----------------------------
DROP TABLE IF EXISTS `product_statistics`;
CREATE TABLE `product_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `stat_date` date NOT NULL,
  `view_count` int NULL DEFAULT 0,
  `order_count` int NULL DEFAULT 0,
  `sales_amount` decimal(10, 2) NULL DEFAULT 0.00,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_date`(`product_id` ASC, `stat_date` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for report_record
-- ----------------------------
DROP TABLE IF EXISTS `report_record`;
CREATE TABLE `report_record`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `post_id` int NOT NULL,
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report_record
-- ----------------------------

-- ----------------------------
-- Table structure for service_banners
-- ----------------------------
DROP TABLE IF EXISTS `service_banners`;
CREATE TABLE `service_banners`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '展示图ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示图标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '展示图描述',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示图URL',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原始文件名',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `image_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片类型：jpg、png、gif等',
  `position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'service_selection' COMMENT '展示位置：service_selection-选择服务下方',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_position`(`position` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务展示图表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_banners
-- ----------------------------
INSERT INTO `service_banners` VALUES (1, '宠物医疗服务展示', '专业的宠物医疗服务，为您的爱宠提供全方位的健康保障', '/uploads/banners/50ca88d4-4d5f-4d3d-bb1c-72f6c0c4c2ac.jpg', 'pet-medical-center.png', 2371842, 'png', 'service_selection', 'active', 1, '2025-10-05 20:04:45', '2025-10-08 21:49:08', 1, NULL);

-- ----------------------------
-- Table structure for service_categories
-- ----------------------------
DROP TABLE IF EXISTS `service_categories`;
CREATE TABLE `service_categories`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类图标',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类颜色',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_categories
-- ----------------------------
INSERT INTO `service_categories` VALUES (1, '医疗服务', '专业的宠物医疗服务，包括体检、疫苗接种、手术等', 'medical', '#409EFF', 'active', 1, '2025-10-05 20:04:45', '2025-10-05 20:04:45');
INSERT INTO `service_categories` VALUES (2, '体检服务', '全面的健康检查服务', 'check', '#67C23A', 'active', 2, '2025-10-05 20:04:45', '2025-10-05 20:04:45');
INSERT INTO `service_categories` VALUES (3, '手术服务', '专业的外科手术治疗', 'surgery', '#E6A23C', 'active', 3, '2025-10-05 20:04:45', '2025-10-05 20:04:45');
INSERT INTO `service_categories` VALUES (4, '急诊服务', '24小时紧急医疗服务', 'emergency', '#F56C6C', 'active', 4, '2025-10-05 20:04:45', '2025-10-05 20:04:45');
INSERT INTO `service_categories` VALUES (5, '美容服务', '专业的宠物美容服务', 'beauty', '#909399', 'active', 5, '2025-10-05 20:04:45', '2025-10-05 20:04:45');
INSERT INTO `service_categories` VALUES (6, '寄养服务', '安全舒适的寄养环境', 'boarding', '#409EFF', 'active', 6, '2025-10-05 20:04:45', '2025-10-05 20:04:45');

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------
INSERT INTO `shopping_cart` VALUES (1, 1, 1, 8, '2025-10-02 01:51:46', '2025-10-02 02:27:20');

-- ----------------------------
-- Table structure for system_announcement
-- ----------------------------
DROP TABLE IF EXISTS `system_announcement`;
CREATE TABLE `system_announcement`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT 1,
  `publish_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_announcement
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user-普通用户, admin-管理员',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `gender` int NULL DEFAULT 0,
  `birthday` date NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `points` int NOT NULL DEFAULT 0 COMMENT '用户积分',
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `member_level` int NULL DEFAULT 1 COMMENT '会员等级：1-普通会员,2-青铜会员,3-白银会员,4-黄金会员,5-钻石会员',
  `total_spent` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '累计消费金额',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '123456', 'admin@pet-home.com', 'admin', '13800138000', NULL, 'Admin', 0, NULL, NULL, 0, 1, '2025-10-04 01:47:55', '2025-10-14 22:18:58', 1, 0.00);

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `address_id` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_address_id`(`address_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_address
-- ----------------------------

-- ----------------------------
-- Table structure for user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `coupon_id` int NOT NULL,
  `status` int NULL DEFAULT 0,
  `used_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_coupon
-- ----------------------------

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `follower_id` int NOT NULL,
  `following_id` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_follow`(`follower_id` ASC, `following_id` ASC) USING BTREE,
  INDEX `idx_follower_id`(`follower_id` ASC) USING BTREE,
  INDEX `idx_following_id`(`following_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_follow
-- ----------------------------

-- ----------------------------
-- Table structure for user_pet
-- ----------------------------
DROP TABLE IF EXISTS `user_pet`;
CREATE TABLE `user_pet`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `pet_id` int NOT NULL,
  `relationship` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'owner',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_pet`(`user_id` ASC, `pet_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_pet
-- ----------------------------

-- ----------------------------
-- Table structure for user_points_level
-- ----------------------------
DROP TABLE IF EXISTS `user_points_level`;
CREATE TABLE `user_points_level`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `level_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `min_points` int NOT NULL,
  `max_points` int NULL DEFAULT NULL,
  `benefits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `status` int NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_min_points`(`min_points` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_points_level
-- ----------------------------

-- ----------------------------
-- Table structure for user_statistics
-- ----------------------------
DROP TABLE IF EXISTS `user_statistics`;
CREATE TABLE `user_statistics`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `stat_date` date NOT NULL,
  `login_count` int NULL DEFAULT 0,
  `post_count` int NULL DEFAULT 0,
  `comment_count` int NULL DEFAULT 0,
  `like_count` int NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `stat_date` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for vaccine_record
-- ----------------------------
DROP TABLE IF EXISTS `vaccine_record`;
CREATE TABLE `vaccine_record`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `pet_id` int NOT NULL,
  `vaccine_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `vaccine_date` date NOT NULL,
  `next_date` date NULL DEFAULT NULL,
  `vet_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `idx_vaccine_date`(`vaccine_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of vaccine_record
-- ----------------------------
INSERT INTO `vaccine_record` VALUES (1, 1, 'Rabies Vaccine', '2025-09-01', '2026-09-01', 'Dr. Li', NULL, '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `vaccine_record` VALUES (2, 2, 'Triple Vaccine', '2025-08-15', '2026-08-15', 'Dr. Wang', NULL, '2025-09-30 23:35:45', '2025-09-30 23:35:45');

-- ----------------------------
-- Table structure for vaccine_reminder
-- ----------------------------
DROP TABLE IF EXISTS `vaccine_reminder`;
CREATE TABLE `vaccine_reminder`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `pet_id` int NOT NULL,
  `vaccine_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `vaccine_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `first_dose_date` date NULL DEFAULT NULL,
  `due_date` date NOT NULL,
  `dose_interval_days` int NULL DEFAULT NULL,
  `total_doses` int NULL DEFAULT NULL,
  `completed_doses` int NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `reminder_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pet_id`(`pet_id` ASC) USING BTREE,
  INDEX `idx_due_date`(`due_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of vaccine_reminder
-- ----------------------------
INSERT INTO `vaccine_reminder` VALUES (1, 1, 'Rabies Vaccine', '核心疫苗', '2025-09-01', '2026-09-01', 365, 1, 0, 'pending', '狂犬疫苗年度接种', NULL, '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `vaccine_reminder` VALUES (2, 2, 'Triple Vaccine', '核心疫苗', '2025-08-15', '2026-08-15', 365, 1, 0, 'pending', '三联疫苗年度接种', NULL, '2025-09-30 23:35:45', '2025-09-30 23:35:45');
INSERT INTO `vaccine_reminder` VALUES (3, 1, '狂犬疫苗', '核心疫苗', '2025-10-01', '2025-10-22', 21, 3, 0, 'pending', NULL, NULL, '2025-10-01 22:32:23', '2025-10-01 22:32:23');
INSERT INTO `vaccine_reminder` VALUES (4, 1, '狂犬疫苗', '核心疫苗', '2025-10-01', '2025-10-22', 21, 3, 0, 'pending', NULL, NULL, '2025-10-01 22:39:54', '2025-10-01 22:39:54');
INSERT INTO `vaccine_reminder` VALUES (5, 1, '狂犬疫苗', '核心疫苗', '2025-10-01', '2025-10-22', 21, 3, 0, 'pending', NULL, NULL, '2025-10-01 23:00:36', '2025-10-01 23:00:36');
INSERT INTO `vaccine_reminder` VALUES (6, 1, '狂犬疫苗', '核心疫苗', '2025-10-01', '2025-10-22', 21, 3, 0, 'pending', NULL, NULL, '2025-10-01 23:56:53', '2025-10-01 23:56:53');
INSERT INTO `vaccine_reminder` VALUES (7, 5, '1\n', '核心疫苗', '2025-10-01', '2025-10-22', 21, 3, 0, 'pending', '用户添加的疫苗提醒', NULL, '2025-10-02 00:44:01', '2025-10-02 00:44:01');
INSERT INTO `vaccine_reminder` VALUES (8, 1, 'Test Vaccine', '??????', '2025-10-03', '2025-10-24', 21, 3, 0, 'pending', 'Test reminder', NULL, '2025-10-03 18:33:12', '2025-10-03 18:33:12');
INSERT INTO `vaccine_reminder` VALUES (9, 1, 'Test Vaccine', '??????', '2025-10-03', '2025-10-24', 21, 3, 0, 'pending', 'Test reminder', NULL, '2025-10-03 18:34:35', '2025-10-03 18:34:35');
INSERT INTO `vaccine_reminder` VALUES (10, 1, 'Test Vaccine', '??????', '2025-10-03', '2025-10-24', 21, 3, 0, 'pending', 'Test reminder', NULL, '2025-10-03 18:34:42', '2025-10-03 18:34:42');
INSERT INTO `vaccine_reminder` VALUES (11, 5, '0', '核心疫苗', '2025-10-03', '2025-10-24', 21, 3, 0, 'pending', '用户添加的疫苗提醒', NULL, '2025-10-03 19:10:22', '2025-10-03 19:10:22');

SET FOREIGN_KEY_CHECKS = 1;
