-- 在线学习平台：用户表 + 课程表 + 学习任务表 + 订单表
-- 注意：Spring Boot 启动时会执行本文件（spring.sql.init.schema-locations）

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  nickname VARCHAR(50) NOT NULL COMMENT '昵称',
  role VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT '角色：admin/student',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

INSERT INTO users (id, username, password, nickname, role) VALUES
(1, 'admin', 'admin123', '管理员', 'admin'),
(2, 'student', '123456', '体验学员', 'student')
ON DUPLICATE KEY UPDATE role=VALUES(role);


CREATE TABLE IF NOT EXISTS courses (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  title VARCHAR(200) NOT NULL COMMENT '课程标题',
  category VARCHAR(50) NOT NULL COMMENT '课程分类',
  level TINYINT NOT NULL COMMENT '课程难度：1=入门 2=进阶 3=高级',
  cover_url VARCHAR(500) NULL COMMENT '封面图URL',
  summary VARCHAR(500) NULL COMMENT '课程简介',
  description TEXT NULL COMMENT '课程详细描述',
  price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '课程价格',
  original_price DECIMAL(10,2) NULL COMMENT '原价',
  view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  buy_count INT NOT NULL DEFAULT 0 COMMENT '购买量',
  total_lessons INT NOT NULL DEFAULT 0 COMMENT '总课时数',
  total_minutes INT NOT NULL DEFAULT 0 COMMENT '总时长（分钟）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '课程状态：0=草稿 1=已发布 2=已下线',
  published_at DATETIME(3) NULL COMMENT '发布时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  PRIMARY KEY (id),
  KEY idx_courses_category_level (category, level),
  KEY idx_courses_status_published (status, published_at),
  KEY idx_courses_created_at (created_at),
  KEY idx_courses_view_count (view_count),
  KEY idx_courses_buy_count (buy_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

CREATE TABLE IF NOT EXISTS learning_tasks (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  learner_id BIGINT NOT NULL COMMENT '学员ID',
  course_id BIGINT NOT NULL COMMENT '关联课程ID',
  title VARCHAR(200) NOT NULL COMMENT '任务标题',
  note VARCHAR(500) NULL COMMENT '任务备注',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=待开始 1=进行中 2=已完成 3=已取消',
  priority TINYINT NOT NULL DEFAULT 2 COMMENT '1=低 2=中 3=高',
  due_date DATE NULL,
  completed_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  CONSTRAINT fk_tasks_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT,
  KEY idx_tasks_learner_course (learner_id, course_id),
  KEY idx_tasks_learner_status_due (learner_id, status, due_date),
  KEY idx_tasks_learner_updated (learner_id, updated_at),
  KEY idx_tasks_learner_completed (learner_id, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务表';

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
  user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  pay_type VARCHAR(20) NULL COMMENT 'alipay/wechat',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0=待支付 1=已支付 2=已取消 3=已退款',
  paid_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_orders_course FOREIGN KEY (course_id) REFERENCES courses(id),
  KEY idx_orders_user (user_id),
  KEY idx_orders_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 插入30门课程（含价格和热度数据）
INSERT INTO courses (id, title, category, level, cover_url, summary, description, price, original_price, view_count, buy_count, total_lessons, total_minutes, status, published_at) VALUES
(1001, 'Spring Boot 实战', '后端开发', 2, '/covers/cover_logo_1001.png', 'Spring Boot 企业级开发实战课程', '', 199.00, 299.00, 963, 52, 40, 2400, 1, '2025-07-15 10:30:00'),
(1002, 'Vue 3 从入门到精通', '前端开发', 1, '/covers/cover_logo_1002.png', 'Vue 3 完整学习路径', '', 149.00, 199.00, 1173, 78, 35, 2100, 1, '2025-08-01 14:00:00'),
(1003, 'MySQL 性能优化', '数据库', 3, '/covers/cover_logo_1003.png', 'MySQL 数据库性能调优实战', '', 299.00, 399.00, 623, 29, 30, 1800, 1, '2025-08-20 09:15:00'),
(1004, 'Java 高级架构师进阶', 'Java', 3, '/covers/cover_logo_1004.png', '深入浅出剖析 Spring/JVM/并发/微服务等底层内核', '', 399.00, 599.00, 1403, 106, 80, 4800, 1, '2025-07-01 08:00:00'),
(1005, 'Python 数据分析实战', 'Python', 2, '/covers/cover_logo_1005.png', '利用 Pandas 与 NumPy 进行商业数据分析与挖掘', '', 179.00, 249.00, 1050, 70, 35, 2100, 1, '2025-09-10 11:00:00'),
(1006, '深度学习与计算机视觉', '人工智能', 3, '/covers/cover_logo_1006.png', '基于 PyTorch 实现图像分类、目标检测与图像生成', '', 499.00, 699.00, 860, 40, 60, 3600, 1, '2025-10-05 16:30:00'),
(1007, 'Hadoop/Spark 大数据架构体系', '大数据', 3, '/covers/cover_logo_1007.png', '构建 PB 级别的高可用分布式数据存储与处理平台', '', 349.00, 499.00, 520, 26, 50, 3000, 1, '2025-10-20 10:00:00'),
(1008, '云原生 Kubernetes 实战', '云计算', 2, '/covers/cover_logo_1008.png', '从 Docker 到 K8s，掌握现代化运维与服务编排', '', 249.00, 349.00, 743, 48, 25, 1500, 1, '2025-11-01 13:45:00'),
(1009, 'Flutter 跨平台高级开发', '移动开发', 2, '/covers/cover_logo_1009.png', '一套代码打通 iOS、Android、Web 多端应用', '', 199.00, 299.00, 660, 36, 40, 2400, 1, '2025-11-15 09:30:00'),
(1010, 'Vue 3 高级组件化实战', '前端开发', 2, '/covers/cover_logo_1010.png', '深入 Composition API，构建高性能企业级 UI 组件库', '', 229.00, 329.00, 920, 63, 30, 1800, 1, '2025-08-10 15:00:00'),
(1011, '微服务架构与云端演进', '云计算', 3, '/covers/cover_logo_1011.png', '全面探讨 Spring Cloud Alibaba 和云原生架构的落地方案', '', 359.00, 499.00, 670, 31, 45, 2700, 1, '2025-12-01 10:20:00'),
(1012, 'Go 语言微服务开发', '后端开发', 2, '/covers/cover_logo_1012.png', '使用 Go 语言及 RPC 框架开发高并发微服务', '', 219.00, 319.00, 563, 28, 40, 2400, 1, '2025-12-15 14:30:00'),
(1013, '深入理解 JVM 原理', 'Java', 3, '/covers/cover_logo_1013.png', '全面剖析 Java 虚拟机运行时内存与垃圾收集机制', '', 329.00, 449.00, 1296, 93, 50, 3000, 1, '2025-07-20 08:45:00'),
(1014, 'React 18 全家桶实战', '前端开发', 2, '/covers/cover_logo_1014.png', 'Hooks 与 Redux 状态管理最佳实践', '', 189.00, 269.00, 980, 58, 45, 2700, 1, '2025-09-01 11:15:00'),
(1015, 'Redis 核心技术与底层原理', '数据库', 3, '/covers/cover_logo_1015.png', 'Redis 分布式缓存与高能调优', '', 279.00, 379.00, 816, 46, 35, 2100, 1, '2025-09-20 16:00:00'),
(1016, '云原生 DevOps 工程师体系', '云计算', 2, '/covers/cover_logo_1016.png', 'Jenkins, GitLab CI/CD 自动化流水线构建', '', 199.00, 299.00, 440, 22, 30, 1800, 1, '2026-01-05 10:00:00'),
(1017, '大模型提示词工程指南', '人工智能', 1, '/covers/cover_logo_1017.png', 'Prompt Engineering 从入门到精通', '', 99.00, 149.00, 1520, 140, 20, 1200, 1, '2025-11-20 09:00:00'),
(1018, 'Flink 流批一体大数据开发', '大数据', 3, '/covers/cover_logo_1018.png', '基于 Flink 构建实时数据湖分析仓库', '', 369.00, 499.00, 393, 17, 60, 3600, 1, '2026-01-10 13:30:00'),
(1019, 'iOS 原生开发精粹', '移动开发', 2, '/covers/cover_logo_1019.png', 'SwiftUI 与 Combine 响应式编程开发', '', 229.00, 329.00, 296, 14, 45, 2700, 1, '2026-01-20 15:45:00'),
(1020, 'Spring Cloud 第四代微服务', '后端开发', 3, '/covers/cover_logo_1020.png', '构建异地多活的高并发分布式电商系统', '', 449.00, 599.00, 1123, 83, 80, 4800, 1, '2025-08-15 08:30:00'),
(1021, 'TypeScript 企业级进阶', '前端开发', 2, '/covers/cover_premium_1021.png', '泛型编程与类型体操，提升代码健壮性', '', 169.00, 249.00, 710, 43, 25, 1500, 1, '2025-09-25 10:00:00'),
(1022, 'PostgreSQL 实战与进阶', '数据库', 2, '/covers/cover_premium_1022.png', '世界上最先进的开源关系型数据库全解', '', 209.00, 299.00, 340, 16, 30, 1800, 1, '2026-02-01 14:15:00'),
(1023, '自然语言处理(NLP)进阶与实操', '人工智能', 3, '/covers/cover_premium_1023.png', '手撕 Transformer 及预训练语言模型应用', '', 459.00, 599.00, 650, 29, 55, 3300, 1, '2025-10-15 09:30:00'),
(1024, 'Android 性能优化大牛班', '移动开发', 3, '/covers/cover_premium_1024.png', '全方位解决卡顿、OOM 与包体积问题', '', 289.00, 399.00, 476, 24, 40, 2400, 1, '2025-12-20 11:00:00'),
(1025, 'Hive 数据仓库实战', '大数据', 2, '/covers/cover_premium_1025.png', '离线数仓架构设计与数据建模指南', '', 239.00, 339.00, 403, 19, 45, 2700, 1, '2026-02-10 10:30:00'),
(1026, 'Docker 容器实操指南', '云计算', 1, '/covers/cover_premium_1026.png', '一小时掌握轻量级容器生命周期管理', '', 79.00, 129.00, 1273, 126, 15, 900, 1, '2025-11-05 08:00:00'),
(1027, '深入解析 Netty 底层架构', '后端开发', 3, '/covers/cover_premium_1027.png', '掌握高性能网络 IO 与并发模型', '', 339.00, 449.00, 573, 30, 50, 3000, 1, '2025-10-25 16:00:00'),
(1028, '前端工程化体系剖析', '前端开发', 3, '/covers/cover_premium_1028.png', 'Webpack / Vite 原理与自定义架构', '', 259.00, 359.00, 770, 52, 35, 2100, 1, '2025-09-15 13:00:00'),
(1029, 'Python 爬虫体系实战', 'Python', 2, '/covers/cover_premium_1029.png', '应对绝大难度反爬虫的分布式抓取与治理', '', 199.00, 279.00, 890, 56, 40, 2400, 1, '2025-10-01 11:30:00'),
(1030, '云架构高可用设计', '云计算', 3, '/covers/cover_premium_1030.png', '大型云端架构师必备的模式与容灾手段', '', 379.00, 499.00, 546, 27, 45, 2700, 1, '2026-02-20 09:45:00')
ON DUPLICATE KEY UPDATE cover_url=VALUES(cover_url), title=VALUES(title), category=VALUES(category), summary=VALUES(summary), price=VALUES(price), original_price=VALUES(original_price), view_count=VALUES(view_count), buy_count=VALUES(buy_count), published_at=VALUES(published_at);

-- 用户-课程关联表（我的课程）——必须在 courses 表之后创建
CREATE TABLE IF NOT EXISTS user_courses (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  progress TINYINT NOT NULL DEFAULT 0 COMMENT '学习进度 0-100',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_user_course (user_id, course_id),
  CONSTRAINT fk_uc_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户课程关联表';


-- ============================================================
-- 用户差异化数据：不同用户拥有不同的课程和学习任务
-- ============================================================

-- admin（管理员）的已购课程：架构/管理类 5 门
INSERT INTO user_courses (user_id, course_id, progress) VALUES
(1, 1004, 85),
(1, 1013, 60),
(1, 1020, 40),
(1, 1027, 20),
(1, 1001, 100)
ON DUPLICATE KEY UPDATE progress=VALUES(progress);

-- student（体验学员）的已购课程：入门/基础类 3 门
INSERT INTO user_courses (user_id, course_id, progress) VALUES
(2, 1002, 68),
(2, 1017, 45),
(2, 1026, 90)
ON DUPLICATE KEY UPDATE progress=VALUES(progress);

-- admin 的学习任务（散点分布 2025-08 ~ 2026-01）
INSERT INTO learning_tasks (id, learner_id, course_id, title, status, created_at) VALUES
(1, 1, 1004, '学习：Java 高级架构师进阶', 1, '2025-08-20 09:30:00'),
(2, 1, 1013, '深入理解 JVM 内存模型', 1, '2025-10-12 14:15:00'),
(3, 1, 1020, '搭建 Spring Cloud 微服务集群', 0, '2025-11-25 10:00:00'),
(4, 1, 1027, '掌握 Netty 核心原理', 0, '2026-01-05 16:40:00'),
(5, 1, 1001, '学习 Spring Boot 基础', 2, '2025-09-03 11:20:00')
ON DUPLICATE KEY UPDATE id=id;

-- student 的学习任务（散点分布 2025-09 ~ 2026-01）
INSERT INTO learning_tasks (id, learner_id, course_id, title, status, created_at) VALUES
(6, 2, 1002, '学习 Vue 3 基础语法', 1, '2025-09-15 19:00:00'),
(7, 2, 1017, '编写第一个 Prompt', 0, '2025-12-01 10:30:00'),
(8, 2, 1026, 'Docker 容器入门实操', 2, '2026-01-18 14:45:00')
ON DUPLICATE KEY UPDATE id=id;
