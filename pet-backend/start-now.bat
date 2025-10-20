@echo off
chcp 65001
cd /d %~dp0
echo ========================================
echo 正在启动后端服务...
echo 当前目录: %cd%
echo ========================================
echo.
java -jar target\pet-home-backend-1.0.0.jar
echo.
echo 服务已停止
pause

