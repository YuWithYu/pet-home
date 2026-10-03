-- 开源发布脱敏脚本
-- ⚠️ 仅对「发布副本库」pet_home_public 执行，切勿直接对本地开发库 pet_home 执行！
-- 完整流程见 docs/database.md「开源发布流程」：
--   1) mysqldump 导出本地库 → database/pet_home.sql
--   2) 建库 pet_home_public 并导入该转储
--   3) 对 pet_home_public 执行本脚本
--   4) mysqldump pet_home_public 覆盖导出 database/pet_home.sql
--   5) DROP DATABASE pet_home_public
--
-- 脱敏范围：手机号 / 邮箱 / 微信 openid、unionid / 收货与预约联系方式 /
--           私信（删除）/ 用户密码哈希统一为演示密码
-- 脱敏后手机号均为可识别的假号段：138x（用户）132x（管理员）137x（地址）
--   136x（订单）135x（预约）134x（员工）133x（门店）

SET @demo_pwd := '$2a$10$DI8/uLGJvRCz61.IuQqd1e/CFrciy8t3qtp4WyPcI4uvM2c9HclgW'; -- 明文 123456

-- ---------- 用户 ----------
UPDATE `user` SET
  password = @demo_pwd,
  phone    = CONCAT('1380000', LPAD(id, 4, '0')),
  email    = CONCAT('user', id, '@example.com'),
  openid   = NULL,
  unionid  = NULL,
  address  = NULL,
  nickname = CONCAT('演示用户', id);
UPDATE `user` SET username = CONCAT('demo_wx_', id) WHERE username LIKE 'wx\_%';

-- ---------- 管理员 ----------
UPDATE admin SET
  password = @demo_pwd,
  phone = CONCAT('1320000', LPAD(id, 4, '0')),
  email = CONCAT('admin', id, '@example.com');

-- ---------- 收货地址 ----------
UPDATE addresses SET
  contact_name  = CONCAT('收货人', id),
  contact_phone = CONCAT('1370000', LPAD(id, 4, '0')),
  street = CONCAT('演示街道', id, '号'),
  detail = CONCAT('演示详细地址', id);

-- ---------- 订单收货信息 ----------
UPDATE pet_order SET
  receiver_name            = CONCAT('收件人', id),
  receiver_phone           = CONCAT('1360000', LPAD(id, 5, '0')),
  receiver_detail_address  = CONCAT('演示地址', id, '号'),
  warehouse_address        = CONCAT('演示仓库地址', id);

-- ---------- 预约联系方式（统一替换为演示值） ----------
UPDATE appointment SET
  contact_name  = CONCAT('联系人', id),
  contact_phone = CONCAT('1350000', LPAD(id, 5, '0')),
  location      = CONCAT('演示地址', id, '号');

UPDATE bookings SET
  contact_name      = CONCAT('联系人', id),
  contact_phone     = CONCAT('1350000', LPAD(id, 5, '0')),
  emergency_contact = CONCAT('紧急联系人', id),
  emergency_phone   = CONCAT('1350001', LPAD(id, 5, '0')),
  address           = CONCAT('演示地址', id, '号');

UPDATE door_cleaning_appointment SET
  contact_name  = CONCAT('联系人', id),
  contact_phone = CONCAT('1350000', LPAD(id, 5, '0'));

UPDATE grooming_appointments SET
  contact_name  = CONCAT('联系人', id),
  contact_phone = CONCAT('1350000', LPAD(id, 5, '0'));

UPDATE hospital_appointments SET
  contact_name  = CONCAT('联系人', id),
  contact_phone = CONCAT('1350000', LPAD(id, 5, '0')),
  member_name   = CONCAT('服务人员', id),
  member_phone  = CONCAT('1340000', LPAD(id, 5, '0')),
  location      = CONCAT('演示地址', id, '号');

UPDATE appointment_change_request SET
  requested_contact_phone = CONCAT('1350000', LPAD(id, 5, '0'));

-- ---------- 员工与门店 ----------
UPDATE service_member SET phone = CONCAT('1340000', LPAD(id, 5, '0'));
UPDATE service_stores    SET phone = CONCAT('1330000', LPAD(id, 5, '0'));

-- ---------- 投诉 ----------
UPDATE complaints SET contact_info = CONCAT('1380001', LPAD(id, 4, '0'));
-- 历史脏数据修复：手机号曾被误存入 user_id 列
UPDATE complaints SET user_id = NULL WHERE user_id > 1000000;

-- ---------- 私信（运行期数据，不随开源分发） ----------
DELETE FROM message;
