@echo off
chcp 65001 > nul
echo ========================================
echo 启动宠物之家后端服务
echo ========================================
echo.

cd /d %~dp0

echo 正在检查Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到Maven，请先安装Maven并配置环境变量
    echo 或使用 mvnw.cmd 代替
    pause
    exit /b 1
)

echo Maven已找到
echo.

echo 正在启动后端服务...
echo 服务端口: 8080
echo 数据库: MySQL (localhost:3306/pet_home)
echo.
echo 请确保MySQL数据库已启动并创建了pet_home数据库！
echo.

mvn spring-boot:run

pause

