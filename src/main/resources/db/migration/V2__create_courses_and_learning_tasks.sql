-- =========================================
-- 在线学习平台 - 数据库表结构设计
-- 版本: v1.0
-- 日期: 2026-01-08
-- 说明: 课程表 + 学习任务表
-- =========================================

-- =========================================
-- 1. 课程表（courses）
-- =========================================
CREATE TABLE IF NOT EXISTS courses (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  title VARCHAR(200) NOT NULL COMMENT '课程标题',
  category VARCHAR(50) NOT NULL COMMENT '课程分类（如：后端开发、前端开发等）',
  level TINYINT NOT NULL COMMENT '课程难度：1=入门 2=进阶 3=高级',
  cover_url VARCHAR(500) NULL COMMENT '封面图URL',
  summary VARCHAR(500) NULL COMMENT '课程简介',
  description TEXT NULL COMMENT '课程详细描述',
  total_lessons INT NOT NULL DEFAULT 0 COMMENT '总课时数',
  total_minutes INT NOT NULL DEFAULT 0 COMMENT '总时长（分钟）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '课程状态：0=草稿 1=已发布 2=已下线',
  published_at DATETIME(3) NULL COMMENT '发布时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  PRIMARY KEY (id),
  KEY idx_courses_category_level (category, level) COMMENT '分类+难度组合索引（支持常见筛选）',
  KEY idx_courses_status_published (status, published_at) COMMENT '状态+发布时间索引（支持已发布课程按时间排序）',
  KEY idx_courses_created_at (created_at) COMMENT '创建时间索引（支持按创建时间排序）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- =========================================
-- 2. 学习任务表（learning_tasks）
-- =========================================
CREATE TABLE IF NOT EXISTS learning_tasks (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  learner_id BIGINT NOT NULL COMMENT '学员ID（多租户隔离字段）',
  course_id BIGINT NOT NULL COMMENT '关联课程ID',
  title VARCHAR(200) NOT NULL COMMENT '任务标题',
  note VARCHAR(500) NULL COMMENT '任务备注/说明',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '任务状态：0=待开始 1=进行中 2=已完成 3=已取消',
  priority TINYINT NOT NULL DEFAULT 2 COMMENT '优先级：1=低 2=中 3=高',
  due_date DATE NULL COMMENT '截止日期',
  completed_at DATETIME(3) NULL COMMENT '完成时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  PRIMARY KEY (id),
  -- 外键约束：防止删除有任务的课程
  CONSTRAINT fk_tasks_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT,
  -- 学员+课程索引（支持查看某学员某课程的任务）
  KEY idx_tasks_learner_course (learner_id, course_id),
  -- 学员+状态+截止日期索引（支持待办/进行中+按截止时间排序）
  KEY idx_tasks_learner_status_due (learner_id, status, due_date),
  -- 学员+更新时间索引（支持最近更新排序）
  KEY idx_tasks_learner_updated (learner_id, updated_at),
  -- 学员+完成时间索引（支持统计完成趋势）
  KEY idx_tasks_learner_completed (learner_id, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务表';

-- =========================================
-- 3. 索引设计说明（面试亮点）
-- =========================================
/*
【课程表索引】
1. idx_courses_category_level：支持"按分类筛选 + 按难度筛选"的组合查询
2. idx_courses_status_published：支持"已发布课程按发布时间排序"的典型列表页查询
3. idx_courses_created_at：支持后台管理"按创建时间排序"的需求

【学习任务表索引】
核心设计原则：任务查询的真实套路是"先按学员过滤，再按其他条件筛选/排序"

1. idx_tasks_learner_course：支持"某学员某课程的所有任务"查询
2. idx_tasks_learner_status_due：支持"待办列表按截止时间排序"的核心场景
   - WHERE learner_id = ? AND status IN (0, 1) ORDER BY due_date ASC
3. idx_tasks_learner_updated：支持"最近更新的任务"查询
4. idx_tasks_learner_completed：支持统计接口按完成时间分组聚合
   - SELECT DATE(completed_at), COUNT(*) FROM learning_tasks WHERE learner_id = ? GROUP BY DATE(completed_at)

【为什么不建 course_id 单独索引？】
- 查询任务时几乎不会"只按课程查，不限学员"
- 如果要看"某课程下所有学员的任务"，可以用 idx_tasks_learner_course 的最左前缀
- 避免索引冗余

【外键约束的取舍】
- 加了 fk_tasks_course 防止误删有任务的课程（业务保护）
- 但没加 learner_id 外键，因为用户表可能在其他系统/未来才做
*/

-- =========================================
-- 4. 种子数据（课程）
-- =========================================
INSERT INTO courses (id, title, category, level, summary, description, total_lessons, total_minutes, status, published_at) VALUES
(1001, 'Java 高级架构师进阶', '后端开发', 3, '系统掌握分布式架构设计', '深入讲解微服务架构、分布式事务、高并发系统设计等核心技术栈，适合有 3 年以上 Java 开发经验的工程师。', 50, 3000, 1, '2026-01-01 00:00:00'),
(1002, 'Spring Boot 实战', '后端开发', 2, '从零构建企业级应用', '通过实战项目学习 Spring Boot 核心特性、数据访问、缓存、安全等内容，快速掌握企业级开发技能。', 30, 1800, 1, '2026-01-05 00:00:00'),
(1003, 'Vue 3 全家桶开发实战', '前端开发', 2, '掌握现代前端开发技术栈', '学习 Vue 3 核心特性、Composition API、Pinia 状态管理、Vue Router 路由、Vite 构建工具等。', 40, 2400, 1, '2026-01-03 00:00:00'),
(1004, 'MySQL 性能优化', '数据库', 3, '深入理解 MySQL 性能调优', '涵盖索引优化、慢查询分析、EXPLAIN 执行计划、InnoDB 存储引擎原理、高并发场景调优等。', 25, 1500, 1, '2026-01-02 00:00:00'),
(1005, 'Redis 缓存实战', '数据库', 2, '构建高性能缓存架构', '学习 Redis 数据结构、缓存策略、持久化、主从复制、哨兵、集群等核心知识。', 20, 1200, 1, '2026-01-04 00:00:00'),
(1006, 'Docker 与 Kubernetes 容器化实战', '运维', 2, '掌握现代容器化技术', '从 Docker 基础到 K8s 集群管理，学习镜像构建、容器编排、服务发现、自动扩缩容等。', 35, 2100, 1, '2026-01-06 00:00:00'),
(1007, 'Python 数据分析', '数据科学', 1, '用 Python 进行数据处理与可视化', '学习 NumPy、Pandas、Matplotlib 等常用库，掌握数据清洗、分析、可视化的基本技能。', 28, 1680, 1, '2026-01-07 00:00:00'),
(1008, '前端性能优化实战', '前端开发', 3, '打造极致用户体验', '深入讲解前端性能优化策略：首屏加载、代码分割、懒加载、CDN、缓存策略、性能监控等。', 18, 1080, 1, '2026-01-08 00:00:00');

-- =========================================
-- 5. 种子数据（学习任务）
-- =========================================
-- 为 learner_id = 1 创建一些示例任务
INSERT INTO learning_tasks (learner_id, course_id, title, note, status, priority, due_date, completed_at) VALUES
-- Java 架构师课程任务
(1, 1001, '完成第1章：微服务架构概述', '重点理解微服务拆分原则', 2, 3, '2026-01-05', '2026-01-05 15:30:00'),
(1, 1001, '完成第2章：Spring Cloud 核心组件', '学习 Eureka、Ribbon、Feign', 2, 3, '2026-01-06', '2026-01-06 18:20:00'),
(1, 1001, '完成第3章：分布式事务', '掌握 Seata 原理与实践', 1, 3, '2026-01-10', NULL),
(1, 1001, '完成第4-5章：RocketMQ 消息队列', '实现异步解耦与最终一致性', 0, 2, '2026-01-15', NULL),

-- Spring Boot 课程任务
(1, 1002, '搭建 Spring Boot 项目脚手架', '配置 Maven、application.yml', 2, 2, '2026-01-07', '2026-01-07 10:00:00'),
(1, 1002, '完成用户 CRUD 功能', '实现 Controller、Service、Repository 三层', 1, 2, '2026-01-09', NULL),
(1, 1002, '集成 MyBatis Plus', '配置分页插件和代码生成器', 0, 2, '2026-01-12', NULL),

-- Vue 3 课程任务
(1, 1003, '学习 Composition API 基础', '理解 ref、reactive、computed、watch', 2, 3, '2026-01-04', '2026-01-04 20:00:00'),
(1, 1003, '实现 Pinia 状态管理', '完成用户状态模块', 1, 2, '2026-01-08', NULL),
(1, 1003, '完成路由配置与导航守卫', '实现权限控制逻辑', 0, 2, '2026-01-11', NULL),

-- MySQL 性能优化课程任务（逾期案例）
(1, 1004, '学习索引原理', 'B+Tree、聚簇索引、覆盖索引', 2, 3, '2026-01-02', '2026-01-03 14:00:00'),
(1, 1004, '练习 EXPLAIN 执行计划分析', '找出慢查询并优化', 0, 3, '2026-01-06', NULL),

-- Redis 缓存课程任务
(1, 1005, '学习 Redis 五大数据结构', 'String、Hash、List、Set、ZSet', 2, 2, '2026-01-03', '2026-01-03 22:00:00'),
(1, 1005, '实现缓存穿透、击穿、雪崩解决方案', '布隆过滤器、互斥锁、随机TTL', 0, 3, '2026-01-13', NULL);

-- =========================================
-- 6. 数据验证查询（可用于测试）
-- =========================================
-- 查看所有课程
-- SELECT id, title, category, level, status FROM courses ORDER BY published_at DESC;

-- 查看学员 1 的所有任务（按截止日期排序）
-- SELECT t.id, t.title, c.title AS course_title, t.status, t.priority, t.due_date, t.completed_at
-- FROM learning_tasks t
-- INNER JOIN courses c ON t.course_id = c.id
-- WHERE t.learner_id = 1
-- ORDER BY t.due_date ASC;

-- 统计学员 1 的任务完成情况
-- SELECT
--   COUNT(*) AS total_tasks,
--   SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS completed_tasks,
--   SUM(CASE WHEN due_date < CURDATE() AND status != 2 THEN 1 ELSE 0 END) AS overdue_tasks
-- FROM learning_tasks
-- WHERE learner_id = 1;
