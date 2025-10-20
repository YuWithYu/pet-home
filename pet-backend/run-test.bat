@echo off
cd /d %~dp0
echo 正在启动后端服务...
java -jar target\pet-home-backend-1.0.0.jar
pause

