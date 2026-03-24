@echo off
chcp 65001 >nul
echo ========================================
echo 在线学习平台 - 后端服务启动脚本
echo ========================================
echo.

REM 使用脚本所在目录，避免写死绝对路径
cd /d "%~dp0"

REM 先确认本机有 mysql 命令行工具（否则后面的“连接失败”会很误导）
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 没找到 mysql 命令行工具
    echo    - 你需要安装 MySQL 客户端，或者把 mysql.exe 加到 PATH
    echo    - 验证方式：在新终端里运行 mysql --version
    pause
    exit /b 1
)

REM 允许用环境变量覆盖；没配就走默认值（本地开发够用）
if "%MYSQL_HOST%"=="" set "MYSQL_HOST=127.0.0.1"
if "%MYSQL_PORT%"=="" set "MYSQL_PORT=3306"
if "%MYSQL_USER%"=="" set "MYSQL_USER=root"
if "%MYSQL_DATABASE%"=="" set "MYSQL_DATABASE=design"
if "%MYSQL_PASSWORD%"=="" (
    echo ⚠️  未设置 MYSQL_PASSWORD，默认使用 root（建议你按自己环境改）
    set "MYSQL_PASSWORD=root"
)

echo [1/3] 检查 MySQL 连接...
mysql -h"%MYSQL_HOST%" -P"%MYSQL_PORT%" -u"%MYSQL_USER%" --password="%MYSQL_PASSWORD%" -e "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ MySQL 连接失败，请确保 MySQL 服务已启动
    echo    - 检查 MySQL 是否运行在 %MYSQL_PORT% 端口
    echo    - 检查 MYSQL_USER / MYSQL_PASSWORD 是否正确
    pause
    exit /b 1
)
echo ✅ MySQL 连接成功

echo.
echo [2/3] 检查数据库...
mysql -h"%MYSQL_HOST%" -P"%MYSQL_PORT%" -u"%MYSQL_USER%" --password="%MYSQL_PASSWORD%" -e "USE %MYSQL_DATABASE%" >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  数据库 '%MYSQL_DATABASE%' 不存在，正在创建...
    mysql -h"%MYSQL_HOST%" -P"%MYSQL_PORT%" -u"%MYSQL_USER%" --password="%MYSQL_PASSWORD%" -e "CREATE DATABASE IF NOT EXISTS %MYSQL_DATABASE% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    echo ✅ 数据库创建成功
) else (
    echo ✅ 数据库已存在
)

echo.
echo [3/3] 启动 Spring Boot 服务...
echo    - 端口：3001
echo    - API 地址：http://localhost:3001/api/v1
echo    - 按 Ctrl+C 停止服务
echo.

mvn spring-boot:run

pause
