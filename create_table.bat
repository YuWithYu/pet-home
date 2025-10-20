@echo off
echo 正在创建service_config表...
mysql -u root -p pet_home < create_service_config_table.sql
echo 表创建完成！
pause
