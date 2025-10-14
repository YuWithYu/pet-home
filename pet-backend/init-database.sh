#!/bin/bash

echo "正在初始化宠物家数据库..."

# 设置数据库连接参数
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="pet_home"
DB_USER="root"
DB_PASS="123456"

echo "创建完整的宠物家数据库（包含中国行政区划数据）..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS < database/pet.home.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "================================================"
    echo "数据库初始化完成！"
    echo "================================================"
    echo "包含功能："
    echo "- 用户管理系统"
    echo "- 宠物档案管理"
    echo "- 宠物商城系统"
    echo "- 宠物社区功能"
    echo "- 宠物服务预约"
    echo "- 消息通知系统"
    echo "- 数据统计分析"
    echo "- AI推荐系统"
    echo "- 地图服务功能"
    echo "- 积分等级系统"
    echo "- 中国行政区划三级数据（省/市/县）"
    echo "================================================"
    echo "数据库名称：$DB_NAME"
    echo "表数量：约30个业务表"
    echo "行政区划：34个省 + 主要城市 + 区县"
    echo "================================================"
else
    echo ""
    echo "数据库初始化失败！"
    echo "请检查："
    echo "1. MySQL服务是否启动"
    echo "2. 数据库连接参数是否正确"
    echo "3. 用户权限是否足够"
    echo ""
    echo "当前连接参数："
    echo "主机：$DB_HOST"
    echo "端口：$DB_PORT"
    echo "数据库：$DB_NAME"
    echo "用户：$DB_USER"
fi
