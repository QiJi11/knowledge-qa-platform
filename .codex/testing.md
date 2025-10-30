## 2026-01-09 测试记录

### 单元测试
- 通过：CourseControllerTest（`mvn -q test -Dtest=CourseControllerTest`）

### 集成测试（需要 MySQL）
- 通过：CourseIntegrationTest（`mvn -q test -Dtest=CourseIntegrationTest`，MYSQL_PASSWORD=root）
- 通过：LearningTaskIntegrationTest（`mvn -q test -Dtest=LearningTaskIntegrationTest`，MYSQL_PASSWORD=root）

### 全量
- 通过：`mvn -q test`（MYSQL_PASSWORD=root）