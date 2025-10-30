#!/bin/bash

echo "========================================"
echo "在线学习平台 - 后端服务启动脚本"
echo "========================================"
echo ""

# 使用脚本所在目录，避免写死绝对路径
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# 允许用环境变量覆盖；没配就走默认值（本地开发够用）
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_DATABASE="${MYSQL_DATABASE:-design}"
if [ -z "${MYSQL_PASSWORD:-}" ]; then
  echo "⚠️  未设置 MYSQL_PASSWORD，默认使用 root（建议你按自己环境改）"
  MYSQL_PASSWORD="root"
fi

if ! command -v mysql >/dev/null 2>&1; then
  echo "❌ 没找到 mysql 命令行工具"
  echo "   - 你需要安装 MySQL 客户端，或者把 mysql 加到 PATH"
  echo "   - 验证方式：mysql --version"
  exit 1
fi

echo "[1/3] 检查 MySQL 连接..."
if mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -e "SELECT 1" >/dev/null 2>&1; then
    echo "✅ MySQL 连接成功"
else
    echo "❌ MySQL 连接失败，请确保 MySQL 服务已启动"
    echo "   - 检查 MySQL 是否运行在 ${MYSQL_PORT} 端口"
    echo "   - 检查 MYSQL_USER / MYSQL_PASSWORD 是否正确"
    exit 1
fi

echo ""
echo "[2/3] 检查数据库..."
if mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -e "USE ${MYSQL_DATABASE}" >/dev/null 2>&1; then
    echo "✅ 数据库已存在"
else
    echo "⚠️  数据库 '${MYSQL_DATABASE}' 不存在，正在创建..."
    mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
    echo "✅ 数据库创建成功"
fi

echo ""
echo "[3/3] 启动 Spring Boot 服务..."
echo "   - 端口：3001"
echo "   - API 地址：http://localhost:3001/api/v1"
echo "   - 按 Ctrl+C 停止服务"
echo ""

mvn spring-boot:run
