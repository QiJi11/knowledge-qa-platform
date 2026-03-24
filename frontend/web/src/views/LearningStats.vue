<template>
  <div class="stats-page">
    <div class="container">
      <h1 class="page-title">学习统计</h1>

      <!-- 未登录提示 -->
      <div v-if="!isLoggedIn" class="card login-prompt">
        <p class="login-text">🔒 请先登录后再查看学习统计</p>
        <button class="btn btn-primary" @click="$router.push('/login')">去登录</button>
      </div>

      <div v-if="isLoggedIn" class="card">
        <div class="filters">
          <label class="field">
            <span class="label">开始日期</span>
            <input v-model="from" type="date" class="input" />
          </label>
          <label class="field">
            <span class="label">结束日期</span>
            <input v-model="to" type="date" class="input" />
          </label>
          <button class="btn btn-primary" :disabled="loading" @click="refresh">
            刷新
          </button>
        </div>
        <p class="tip">提示：为了避免“今天/昨天”带来结果漂移，默认给了固定日期范围（你也可以改）。</p>
      </div>

      <div v-if="loading" class="muted mt">加载中...</div>

      <template v-else-if="isLoggedIn">
        <div v-if="overview" class="grid mt">
          <div class="stat">
            <div class="k">任务总数</div>
            <div class="v">{{ overview.summary.totalTasks }}</div>
          </div>
          <div class="stat">
            <div class="k">已完成</div>
            <div class="v">{{ overview.summary.completedTasks }}</div>
          </div>
          <div class="stat">
            <div class="k">完成率</div>
            <div class="v">{{ percent(overview.summary.completionRate) }}</div>
          </div>
          <div class="stat">
            <div class="k">逾期任务</div>
            <div class="v">{{ overview.summary.overdueTasks }}</div>
          </div>
          <div class="stat">
            <div class="k">进行中</div>
            <div class="v">{{ overview.summary.inProgressTasks }}</div>
          </div>
        </div>

        <div v-if="overview" class="card mt">
          <h2 class="card-title">按状态</h2>
          <div class="badges">
            <span class="badge s0">待开始：{{ overview.byStatus.pending }}</span>
            <span class="badge s1">进行中：{{ overview.byStatus.inProgress }}</span>
            <span class="badge s2">已完成：{{ overview.byStatus.completed }}</span>
            <span class="badge s3">已取消：{{ overview.byStatus.cancelled }}</span>
          </div>

          <h2 class="card-title mt2">按优先级</h2>
          <div class="badges">
            <span class="badge p1">低：{{ overview.byPriority.low }}</span>
            <span class="badge p2">中：{{ overview.byPriority.medium }}</span>
            <span class="badge p3">高：{{ overview.byPriority.high }}</span>
          </div>
        </div>

        <div v-if="byCourse" class="card mt">
          <h2 class="card-title">按课程聚合</h2>
          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>课程</th>
                  <th>分类</th>
                  <th>总任务</th>
                  <th>已完成</th>
                  <th>完成率</th>
                  <th>逾期</th>
                  <th>平均完成天数</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in byCourse.courses" :key="c.courseId">
                  <td>{{ c.courseTitle }}</td>
                  <td>{{ c.category }}</td>
                  <td>{{ c.totalTasks }}</td>
                  <td>{{ c.completedTasks }}</td>
                  <td>{{ percent(c.completionRate) }}</td>
                  <td>{{ c.overdueTasks }}</td>
                  <td>{{ c.avgCompletionDays == null ? '-' : c.avgCompletionDays.toFixed(1) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="trend" class="card mt">
          <h2 class="card-title">趋势（创建/完成）</h2>
          <div class="chart-container" style="height: 300px; width: 100%;">
            <v-chart :option="chartOption" autoresize />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'

use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
])

import {
  getByCourseStats,
  getOverviewStats,
  getTrendStats,
  type ByCourseStats,
  type OverviewStats,
  type TrendStats,
} from '@/api/stats'

const learnerId = 1

const loading = ref(false)
const from = ref('2026-01-01')
const to = ref('2026-12-31')

const overview = ref<OverviewStats | null>(null)
const byCourse = ref<ByCourseStats | null>(null)
const trend = ref<TrendStats | null>(null)

function percent(rate: number) {
  const v = Math.round(rate * 1000) / 10
  return `${v}%`
}

async function refresh() {
  loading.value = true
  try {
    const [o, b, t] = await Promise.all([
      getOverviewStats(learnerId, { from: from.value, to: to.value }),
      getByCourseStats(learnerId, { from: from.value, to: to.value, sortBy: 'totalTasks', order: 'desc' }),
      getTrendStats(learnerId, { from: from.value, to: to.value, granularity: 'day' }),
    ])
    overview.value = o
    byCourse.value = b
    trend.value = t
  } catch (e: any) {
    ElMessage.error(`加载失败：${e?.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

const chartOption = computed(() => {
  if (!trend.value) return {}
  const dates = trend.value.dataPoints.map(p => p.date)
  const created = trend.value.dataPoints.map(p => p.createdCount)
  const completed = trend.value.dataPoints.map(p => p.completedCount)

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['创建数', '完成数'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '创建数',
        type: 'line',
        data: created,
        smooth: true,
        itemStyle: { color: '#3b82f6' },
      },
      {
        name: '完成数',
        type: 'line',
        data: completed,
        smooth: true,
        itemStyle: { color: '#10b981' },
      }
    ]
  }
})

onMounted(() => {
  if (isLoggedIn.value) {
    refresh()
  }
})
</script>

<style scoped>
.stats-page {
  padding: 24px 0 60px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--card-shadow);
}

.mt {
  margin-top: 16px;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 13px;
  color: var(--text-secondary);
}

.input {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}

.tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.muted {
  color: var(--text-tertiary);
  font-size: 13px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.stat {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--card-shadow);
}

.k {
  color: var(--text-secondary);
  font-size: 13px;
}

.v {
  font-size: 26px;
  font-weight: 800;
  margin-top: 6px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 10px;
}

.mt2 {
  margin-top: 16px;
}

.badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.badge.s0 {
  background: #eef2ff;
  color: #1d4ed8;
}
.badge.s1 {
  background: #ecfeff;
  color: #0e7490;
}
.badge.s2 {
  background: #ecfdf5;
  color: #047857;
}
.badge.s3 {
  background: #fef2f2;
  color: #b91c1c;
}

.badge.p1 {
  background: #f3f4f6;
  color: #111827;
}
.badge.p2 {
  background: #fff7ed;
  color: #9a3412;
}
.badge.p3 {
  background: #fefce8;
  color: #854d0e;
}

.table-wrap {
  overflow: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th,
.table td {
  padding: 10px 8px;
  border-bottom: 1px solid var(--border-color);
  vertical-align: top;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.login-prompt {
  text-align: center;
  padding: 48px 20px;
}

.login-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

