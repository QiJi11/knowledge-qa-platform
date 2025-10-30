-- 测试环境专用：每次启动都重建表，避免历史表结构影响集成测试

DROP TABLE IF EXISTS learning_tasks;
DROP TABLE IF EXISTS courses;

CREATE TABLE courses (
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
  KEY idx_courses_category_level (category, level) COMMENT '分类+难度组合索引',
  KEY idx_courses_status_published (status, published_at) COMMENT '状态+发布时间索引',
  KEY idx_courses_created_at (created_at) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

CREATE TABLE learning_tasks (
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
  CONSTRAINT fk_tasks_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT,
  KEY idx_tasks_learner_course (learner_id, course_id),
  KEY idx_tasks_learner_status_due (learner_id, status, due_date),
  KEY idx_tasks_learner_updated (learner_id, updated_at),
  KEY idx_tasks_learner_completed (learner_id, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务表';