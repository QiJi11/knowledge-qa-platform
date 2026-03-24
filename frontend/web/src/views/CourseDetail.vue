<template>
  <div class="course-detail-page">
    <div v-if="loading" class="container">
      <div class="skeleton" style="height: 40px; width: 60%; margin-bottom: 20px;"></div>
      <div class="skeleton" style="height: 300px; margin-bottom: 20px;"></div>
    </div>

    <div v-else-if="course" class="container">
      <h1 class="course-title">{{ course.title }}</h1>
      
      <div class="course-content">
        <div class="course-main">
          <div class="course-cover">
            <img v-if="course.coverUrl" :src="course.coverUrl" :alt="course.title" class="cover-image" />
            <div v-else class="cover-placeholder"></div>
          </div>
          
          <div class="course-info">
            <!-- 课程基本信息标签 -->
            <div class="course-tags">
              <span class="tag tag-category">{{ course.category }}</span>
              <span class="tag tag-level">L{{ course.level }} {{ levelLabel(course.level) }}</span>
              <span class="tag tag-lessons">共 {{ course.totalLessons || 0 }} 课时</span>
              <span class="tag tag-duration">约 {{ Math.round((course.totalMinutes || 0) / 60) }} 小时</span>
            </div>

            <h2>课程介绍</h2>
            <p class="course-summary">{{ course.summary || '暂无简介' }}</p>
            <p class="course-description">{{ course.description || '暂无详细介绍' }}</p>
            
            <!-- 课程大纲 -->
            <h2>课程大纲</h2>
            <div class="course-outline">
              <div v-for="(item, idx) in courseOutline" :key="idx" class="outline-item">
                <span class="outline-index">{{ String(idx + 1).padStart(2, '0') }}</span>
                <span class="outline-text">{{ item }}</span>
              </div>
            </div>

            <!-- 适合人群 -->
            <h2>适合人群</h2>
            <ul class="audience-list">
              <li v-for="(item, idx) in courseAudience" :key="idx">{{ item }}</li>
            </ul>

            <!-- 课程详细信息 -->
            <div class="course-meta">
              <div class="meta-item">
                <strong>分类：</strong>{{ course.category }}
              </div>
              <div class="meta-item">
                <strong>等级：</strong>L{{ course.level }}
              </div>
              <div class="meta-item">
                <strong>总课时：</strong>{{ course.totalLessons || 0 }}
              </div>
              <div class="meta-item">
                <strong>总时长：</strong>{{ course.totalMinutes || 0 }} 分钟
              </div>
              <div class="meta-item">
                <strong>发布时间：</strong>{{ formatDate(course.publishedAt || course.createdAt) }}
              </div>
            </div>
          </div>
        </div>

        <div class="course-sidebar">
          <div class="action-card">
            <div class="price-display" v-if="course.price && course.price > 0">
              <span class="current-price">¥{{ course.price }}</span>
              <span class="orig-price" v-if="course.originalPrice && course.originalPrice > course.price">¥{{ course.originalPrice }}</span>
            </div>
            <div class="price-display" v-else>
              <span class="current-price free">免费</span>
            </div>
            <div class="stats-row">
              <span>🔥 {{ course.viewCount || 0 }} 人浏览</span>
              <span>📚 {{ course.buyCount || 0 }} 人购买</span>
            </div>
            <button class="btn-primary btn-full btn-buy" @click="handleBuy" v-if="course.price && course.price > 0">
              立即购买
            </button>
            <button class="btn-primary btn-full" @click="handleAddToPlan">
              加入学习计划
            </button>
            <button class="btn-outline btn-full" @click="goToPlan">
              查看学习计划
            </button>
            <p class="action-tip" v-if="!isLoggedIn">💡 提示：请先<router-link to="/login">登录</router-link>后再购买或加入学习计划。</p>
            <p class="action-tip" v-else>✅ 已登录，可直接操作。</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="container">
      <p class="error-message">课程不存在或已下架</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCourseDetail, type Course } from '@/api/course'
import { createLearningTask } from '@/api/learningTask'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const course = ref<Course | null>(null)

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const levelLabel = (level: number) => {
  const labels: Record<number, string> = { 1: '入门', 2: '进阶', 3: '高级' }
  return labels[level] || ''
}

// 课程大纲（根据课程生成）
const outlineMap: Record<string, string[]> = {
  'Java': [
    '分布式系统架构设计原理', '微服务拆分与服务治理策略',
    'Spring Cloud Alibaba 核心组件实战', 'Redis 缓存架构与高可用方案',
    '消息队列 Kafka 集群搭建与调优', 'MySQL 分库分表与读写分离',
    '高并发场景下的限流熔断降级', '容器化部署与 CI/CD 自动化',
  ],
  '前端开发': [
    'Vue 3 响应式系统与 Composition API 深度解析', 'Vite 构建工具链配置与优化',
    'TypeScript 在 Vue 3 项目中的最佳实践', 'Element Plus 组件库高级定制',
    'Pinia 状态管理与持久化策略', 'Vue Router 路由守卫与权限控制',
    '前端性能优化：懒加载、虚拟列表与缓存', '企业级中后台项目实战演练',
  ],
  'Python': [
    'Python 数据处理环境搭建', 'NumPy 数组运算与线性代数基础',
    'Pandas 数据清洗与预处理实战', '数据可视化：Matplotlib 与 Seaborn',
    '金融数据案例：股票趋势分析', '商业数据案例：用户行为分析',
    '机器学习基础：Scikit-learn 入门', '数据分析报告自动化生成',
  ],
  '大数据': [
    'Hadoop 生态系统全景概述', 'HDFS 分布式文件系统原理与操作',
    'MapReduce 编程模型与实战', 'YARN 资源调度与集群管理',
    'Hive 数据仓库查询优化', 'Spark 计算引擎快速入门',
    '大数据实时处理：Flink 基础', '高可用 Hadoop 集群生产部署',
  ],
  '人工智能': [
    '深度学习数学基础：线性代数与概率论', '神经网络核心原理：前向传播与反向传播',
    'CNN 卷积神经网络与图像分类', 'RNN/LSTM 与序列建模',
    'Transformer 架构详解', 'PyTorch 框架实战入门',
    '自然语言处理：文本分类与情感分析', '大模型微调与部署实战',
  ],
  '云计算': [
    'Docker 容器核心概念与安装', 'Dockerfile 编写与镜像构建最佳实践',
    'Docker Compose 多容器编排', 'Kubernetes 架构与核心组件解析',
    'K8s 资源对象：Pod, Service, Deployment', 'K8s 集群监控与日志收集',
    'CI/CD 流水线：Jenkins + K8s', '云原生微服务部署综合实战',
  ],
  '数据库': [
    'MySQL 体系结构与存储引擎对比', 'InnoDB 事务原理与隔离级别',
    'B+Tree 索引原理与索引优化策略', 'SQL 慢查询分析与 EXPLAIN 详解',
    '锁机制：行锁、表锁与死锁排查', '主从复制与读写分离架构',
    '分库分表方案设计与 ShardingSphere', '海量数据归档与数据库运维实战',
  ],
  '移动开发': [
    'Dart 语言核心语法与异步编程', 'Flutter Widget 体系与布局系统',
    '状态管理方案对比：Provider vs Riverpod', '网络请求与 RESTful API 对接',
    '本地存储与 SQLite 数据库', '原生插件开发与平台通道',
    'Flutter 动画与自定义绘制', 'iOS 与 Android 双平台打包发布',
  ],
}

const audienceMap: Record<string, string[]> = {
  'Java': ['有 1-3 年 Java 开发经验，想进阶架构师的工程师', '准备面试大厂高级岗位的求职者', '想系统掌握分布式和微服务体系的开发者'],
  '前端开发': ['有 HTML/CSS/JS 基础，想转型 Vue 3 的前端开发者', '需要从 Vue 2 迁移到 Vue 3 的团队成员', '希望构建企业级中后台项目的全栈工程师'],
  'Python': ['零基础想入门数据分析的初学者', '需要用数据支撑决策的产品经理和运营人员', '想转行数据分析师的其他行业从业者'],
  '大数据': ['有 Java 或 Python 基础，想进入大数据领域的开发者', '需要搭建数据平台的运维和架构师', '对分布式计算感兴趣的计算机专业学生'],
  '人工智能': ['有 Python 基础，想深入理解深度学习的开发者', '准备从事 AI 研发工作的研究生和求职者', '希望掌握大模型技术的技术管理者'],
  '云计算': ['想掌握容器化部署技术的后端开发者', '需要建设 DevOps 体系的运维工程师', '对云原生架构感兴趣的技术管理者'],
  '数据库': ['日常使用 MySQL 但缺乏深入理解的后端开发者', '遇到数据库性能瓶颈需要调优的 DBA', '准备面试中涉及数据库原理问题的求职者'],
  '移动开发': ['想用一套代码开发 iOS 和 Android 应用的开发者', '从原生开发转型跨平台技术的移动端工程师', '想快速原型验证产品想法的创业者'],
}

const courseOutline = computed(() => {
  if (!course.value) return []
  return outlineMap[course.value.category] || ['课程内容精心准备中，敬请期待...']
})

const courseAudience = computed(() => {
  if (!course.value) return []
  return audienceMap[course.value.category] || ['所有对该领域感兴趣的学习者']
})

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const handleBuy = () => {
  if (!course.value) return
  if (!isLoggedIn.value) {
    alert('请先登录后再购买课程')
    router.push(`/login?redirect=/course/${course.value.id}`)
    return
  }
  router.push(`/order/confirm/${course.value.id}`)
}

const goToPlan = () => {
  router.push('/plan')
}

const handleAddToPlan = async () => {
  if (!course.value) return
  if (!isLoggedIn.value) {
    alert('请先登录后再加入学习计划')
    router.push(`/login?redirect=/course/${course.value.id}`)
    return
  }
  try {
    await createLearningTask(Number(localStorage.getItem('userId')) || 1, {
      courseId: course.value.id,
      title: `学习：${course.value.title}`,
      priority: 2,
      dueDate: null,
      note: null,
    })
    alert('已加入学习计划 ✅\n你可以去「学习计划」里查看/修改状态。')
    router.push('/plan')
  } catch (e: any) {
    alert(`加入失败：${e?.message || '未知错误'}`)
  }
}

const fetchCourseDetail = async () => {
  try {
    loading.value = true
    const id = route.params.id as string
    course.value = await getCourseDetail(id)
  } catch (error) {
    console.error('获取课程详情失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCourseDetail()
})
</script>

<style scoped>
.course-detail-page {
  padding: 40px 0;
  min-height: 60vh;
}

.course-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 30px;
}

.course-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 30px;
}

.course-main {
  background: white;
  border-radius: 12px;
  padding: 30px;
}

.course-cover {
  margin-bottom: 30px;
}

.cover-placeholder {
  width: 100%;
  height: 400px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
}

.cover-image {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  border-radius: 8px;
}

.course-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.tag {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.tag-category { background: #e8f4fd; color: #1976d2; }
.tag-level    { background: #fef3e2; color: #e65100; }
.tag-lessons  { background: #e8f5e9; color: #2e7d32; }
.tag-duration { background: #f3e5f5; color: #7b1fa2; }

.course-info h2 {
  font-size: 22px;
  margin-bottom: 16px;
  margin-top: 30px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--primary-color);
  display: inline-block;
}

.course-info h2:first-of-type {
  margin-top: 0;
}

.course-summary {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 20px;
  line-height: 1.6;
}

.course-description {
  font-size: 15px;
  color: var(--text-primary);
  line-height: 1.8;
  margin-bottom: 10px;
}

.course-outline {
  margin-bottom: 10px;
}

.outline-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.2s;
}

.outline-item:hover {
  background: #e8f4fd;
  transform: translateX(4px);
}

.outline-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: var(--primary-color);
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  margin-right: 14px;
  flex-shrink: 0;
}

.outline-text {
  font-size: 15px;
  color: var(--text-primary);
}

.audience-list {
  list-style: none;
  padding: 0;
  margin-bottom: 10px;
}

.audience-list li {
  padding: 10px 16px 10px 36px;
  position: relative;
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.audience-list li::before {
  content: '✓';
  position: absolute;
  left: 10px;
  color: #43a047;
  font-weight: 700;
}

.course-meta {
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.meta-item {
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--text-secondary);
}

.course-sidebar {
  position: sticky;
  top: 84px;
  height: fit-content;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: var(--card-shadow);
  text-align: center;
}

.btn-full {
  width: 100%;
  padding: 14px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-outline {
  margin-top: 12px;
  background: white;
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
}

.btn-outline:hover {
  background: #eaf2ff;
}

.action-tip {
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.price-display {
  margin-bottom: 12px;
  text-align: center;
}

.current-price {
  font-size: 32px;
  font-weight: 700;
  color: #ff4d4f;
}

.current-price.free {
  color: #52c41a;
}

.orig-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.btn-buy {
  background: #ff4d4f;
  margin-bottom: 10px;
}

.btn-buy:hover {
  background: #ff2d2d;
}

.error-message {
  text-align: center;
  padding: 80px 0;
  font-size: 18px;
  color: var(--text-tertiary);
}

@media (max-width: 968px) {
  .course-content {
    grid-template-columns: 1fr;
  }
  
  .course-sidebar {
    position: static;
  }
}
</style>
