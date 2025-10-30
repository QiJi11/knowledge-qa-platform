-- 集成测试数据库初始化脚本
-- 运行方式：mysql -u root -p < setup-test-db.sql

CREATE DATABASE IF NOT EXISTS design_test CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 授权（如果需要）
-- GRANT ALL PRIVILEGES ON design_test.* TO 'root'@'localhost';
-- FLUSH PRIVILEGES;
