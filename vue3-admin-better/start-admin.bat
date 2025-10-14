@echo off
echo 正在启动管理员前端...
cd /d %~dp0
call npm run serve:rspack
pause

