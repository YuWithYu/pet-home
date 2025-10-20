@echo off
chcp 65001
echo ========================================
echo 启动后端服务（调试模式）
echo ========================================
cd /d %~dp0
echo 当前目录: %cd%
echo.
echo 正在启动服务...
java -jar target\pet-home-backend-1.0.0.jar
pause

