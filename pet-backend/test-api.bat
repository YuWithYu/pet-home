@echo off
echo Testing user registration...
echo.

REM 使用PowerShell发送POST请求
powershell -Command ^
"$body = @{username='testuser'; password='testpass'; email='test@example.com'; phone='13800138000'} | ConvertTo-Json; ^
Invoke-WebRequest -Uri 'http://localhost:8080/api/users/register' ^
-Method POST ^
-ContentType 'application/json' ^
-Body $body ^
-OutFile response.txt"

echo Response saved to response.txt
echo.

echo Testing user login...
echo.

powershell -Command ^
"$body = @{username='testuser'; password='testpass'} | ConvertTo-Json; ^
Invoke-WebRequest -Uri 'http://localhost:8080/api/users/login' ^
-Method POST ^
-ContentType 'application/json' ^
-Body $body ^
-OutFile login_response.txt"

echo Login response saved to login_response.txt
