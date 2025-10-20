-- 重命名appointment表为door_cleaning_appointment表
USE pet_home;

-- 1. 重命名appointment表为door_cleaning_appointment
RENAME TABLE appointment TO door_cleaning_appointment;

-- 2. 更新表注释
ALTER TABLE door_cleaning_appointment COMMENT = '上门铲屎服务预约表';

-- 3. 确保所有door-cleaning相关的记录都在这个表中
UPDATE door_cleaning_appointment 
SET service_type = 'door-cleaning' 
WHERE service_type IS NULL OR service_type = '';

-- 4. 显示表结构确认
SHOW CREATE TABLE door_cleaning_appointment;
